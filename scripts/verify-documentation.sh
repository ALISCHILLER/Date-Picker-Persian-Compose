#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 - "$ROOT_DIR" <<'PY'
from pathlib import Path
import re
import sys

root = Path(sys.argv[1]).resolve()
markdown_files = [p for p in root.rglob("*.md") if not any(part in {"build", ".gradle"} for part in p.parts)]
link_pattern = re.compile(r"!?\[[^\]]*\]\(([^)]+)\)")
errors: list[str] = []

for document in markdown_files:
    text = document.read_text(encoding="utf-8")
    for raw_target in link_pattern.findall(text):
        target = raw_target.strip().split()[0].strip("<>")
        if not target or target.startswith(("http://", "https://", "mailto:", "#")):
            continue
        path_part = target.split("#", 1)[0]
        if not path_part:
            continue
        resolved = (document.parent / path_part).resolve()
        try:
            resolved.relative_to(root)
        except ValueError:
            errors.append(f"{document.relative_to(root)}: link escapes repository: {target}")
            continue
        if not resolved.exists():
            errors.append(f"{document.relative_to(root)}: missing link target: {target}")

readme = (root / "README.md").read_text(encoding="utf-8")
required = [
    "# نسخه فارسی",
    "# English Version",
    "docs/screenshots/app-showcase.png",
    "not presented as a verified emulator screenshot",
]
for marker in required:
    if marker not in readme:
        errors.append(f"README.md: missing required marker: {marker}")

if errors:
    raise SystemExit("Documentation verification failed:\n" + "\n".join(f"- {e}" for e in errors))

print(f"Documentation links and README policy verified across {len(markdown_files)} Markdown files.")
PY
