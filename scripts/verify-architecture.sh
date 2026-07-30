#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CORE_DIR="$ROOT_DIR/calendar-core/src"

fail() {
  printf 'Architecture verification failed: %s\n' "$1" >&2
  exit 1
}

if grep -RInE '^import (android\.|androidx\.|kotlinx\.coroutines)' "$CORE_DIR" --include='*.kt'; then
  fail 'calendar-core must remain free of Android, AndroidX, Compose, and coroutine dependencies.'
fi

if grep -RInE 'GlobalScope|TODO\(\)|error\("Not implemented"\)|fallbackToDestructiveMigration' \
  "$ROOT_DIR/app/src" "$ROOT_DIR/calendar/src" "$CORE_DIR" --include='*.kt'; then
  fail 'forbidden runtime placeholder or unowned concurrency API found.'
fi

if grep -RInE '(api[_-]?key|secret|token|password)[[:space:]]*=[[:space:]]*["'"'][^"'"']+["'"']' \
  "$ROOT_DIR" \
  --include='*.kt' --include='*.kts' --include='*.properties' --exclude='local.properties'; then
  fail 'possible hard-coded secret found.'
fi


if grep -RIn 'com\.msa\.persioncalendar' "$ROOT_DIR/app" --include='*.kt' --include='*.kts'; then
  fail 'misspelled sample application package must not return.'
fi

if find "$ROOT_DIR/app/src" "$ROOT_DIR/calendar/src" -name 'baseline-prof.txt' -type f | grep -q .; then
  fail 'unmeasured baseline profile placeholders must not be shipped.'
fi

if grep -RInE 'excludes.*META-INF/(LICENSE|NOTICE)|META-INF/(LICENSE|NOTICE).*excludes' \
  "$ROOT_DIR/app/build.gradle.kts" "$ROOT_DIR/calendar/build.gradle.kts"; then
  fail 'license and notice metadata must be merged, not discarded.'
fi

python3 - "$ROOT_DIR" <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1])
android = "{http://schemas.android.com/apk/res/android}"

for module in ("app", "calendar"):
    base = root / module / "src/main/res/values/strings.xml"
    fa = root / module / "src/main/res/values-fa/strings.xml"
    if not base.exists() or not fa.exists():
        raise SystemExit(f"missing bilingual resources in {module}")

    def names(path: Path) -> set[str]:
        tree = ET.parse(path)
        return {
            node.attrib["name"]
            for node in tree.getroot()
            if node.tag in {"string", "plurals", "string-array"} and "name" in node.attrib
        }

    base_names = names(base)
    fa_names = names(fa)
    if base_names != fa_names:
        raise SystemExit(
            f"resource parity failed for {module}; "
            f"missing fa={sorted(base_names-fa_names)}, missing en={sorted(fa_names-base_names)}"
        )

    import re
    placeholder_pattern = re.compile(r"%(?!%)(?:\d+\$)?[a-zA-Z]")

    def placeholder_signatures(path: Path) -> dict[tuple[str, str, str], tuple[str, ...]]:
        root_node = ET.parse(path).getroot()
        signatures: dict[tuple[str, str, str], tuple[str, ...]] = {}
        for node in root_node:
            name = node.attrib.get("name")
            if not name or node.tag not in {"string", "plurals", "string-array"}:
                continue
            if node.tag == "string":
                text = "".join(node.itertext())
                signatures[(node.tag, name, "value")] = tuple(placeholder_pattern.findall(text))
            else:
                for index, item in enumerate(node.findall("item")):
                    key = item.attrib.get("quantity", str(index))
                    text = "".join(item.itertext())
                    signatures[(node.tag, name, key)] = tuple(placeholder_pattern.findall(text))
        return signatures

    base_signatures = placeholder_signatures(base)
    fa_signatures = placeholder_signatures(fa)
    if base_signatures != fa_signatures:
        differing = sorted(
            key for key in set(base_signatures) | set(fa_signatures)
            if base_signatures.get(key) != fa_signatures.get(key)
        )
        details = [
            f"{key}: en={base_signatures.get(key)}, fa={fa_signatures.get(key)}"
            for key in differing
        ]
        raise SystemExit(
            f"resource placeholder parity failed for {module}:\n" + "\n".join(details)
        )

manifest = ET.parse(root / "app/src/main/AndroidManifest.xml").getroot()
application = manifest.find("application")
if application is None:
    raise SystemExit("sample application manifest is missing <application>")
if application.get(android + "allowBackup") != "false":
    raise SystemExit("sample app must explicitly disable backup")
if application.get(android + "usesCleartextTraffic") != "false":
    raise SystemExit("sample app must explicitly disable cleartext traffic")
if application.get(android + "supportsRtl") != "true":
    raise SystemExit("sample app must explicitly support RTL")
if application.get(android + "localeConfig") != "@xml/locales_config":
    raise SystemExit("sample app must declare its supported locales")
if manifest.findall("uses-permission"):
    names = [n.get(android + "name") for n in manifest.findall("uses-permission")]
    raise SystemExit(f"sample app unexpectedly requests permissions: {names}")
for activity in application.findall("activity"):
    if activity.get(android + "localeConfig") is not None:
        raise SystemExit("localeConfig belongs on <application>, not <activity>")

locale_config = ET.parse(root / "app/src/main/res/xml/locales_config.xml").getroot()
locales = {node.get(android + "name") for node in locale_config.findall("locale")}
if locales != {"en", "fa"}:
    raise SystemExit(f"unexpected locale configuration: {sorted(locales)}")

print("Architecture, manifest, and localization verification passed.")
PY
