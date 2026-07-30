#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="${RELEASE_OUTPUT_DIR:-$ROOT_DIR/release-artifacts}"
PROPERTIES="$ROOT_DIR/gradle.properties"

property() {
  awk -F= -v key="$1" '$1 == key { sub(/^[^=]*=/, ""); print; exit }' "$PROPERTIES" \
    | tr -d '\r' \
    | xargs
}

GROUP_ID="$(property LIBRARY_GROUP)"
CORE_ARTIFACT="$(property LIBRARY_CORE_ARTIFACT)"
COMPOSE_ARTIFACT="$(property LIBRARY_COMPOSE_ARTIFACT)"
VERSION="$(property LIBRARY_VERSION)"
GROUP_PATH="${GROUP_ID//./\/}"

required=(
  "$OUTPUT_DIR/sbom/bom.json"
  "$OUTPUT_DIR/sbom/bom.xml"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION.pom"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION-sources.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION-javadoc.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION.aar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION.pom"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION-sources.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION-javadoc.jar"
  "$OUTPUT_DIR/SHA256SUMS"
  "$OUTPUT_DIR/RELEASE-METADATA.txt"
)

for path in "${required[@]}"; do
  [[ -s "$path" ]] || { echo "Missing required release file: $path" >&2; exit 1; }
done

grep -Fq "group_id=$GROUP_ID" "$OUTPUT_DIR/RELEASE-METADATA.txt"
grep -Fq "core_artifact=$CORE_ARTIFACT" "$OUTPUT_DIR/RELEASE-METADATA.txt"
grep -Fq "compose_artifact=$COMPOSE_ARTIFACT" "$OUTPUT_DIR/RELEASE-METADATA.txt"
grep -Fq "version=$VERSION" "$OUTPUT_DIR/RELEASE-METADATA.txt"

archives=(
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION-sources.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION-javadoc.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION.aar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION-sources.jar"
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION-javadoc.jar"
)
for archive in "${archives[@]}"; do
  unzip -tq "$archive" >/dev/null || { echo "Invalid release archive: $archive" >&2; exit 1; }
done

(
  cd "$OUTPUT_DIR"
  sha256sum --check SHA256SUMS
)

python3 - \
  "$OUTPUT_DIR/sbom/bom.json" \
  "$OUTPUT_DIR/sbom/bom.xml" \
  "$OUTPUT_DIR/maven/$GROUP_PATH/$CORE_ARTIFACT/$VERSION/$CORE_ARTIFACT-$VERSION.pom" \
  "$OUTPUT_DIR/maven/$GROUP_PATH/$COMPOSE_ARTIFACT/$VERSION/$COMPOSE_ARTIFACT-$VERSION.pom" \
  "$GROUP_ID" "$CORE_ARTIFACT" "$COMPOSE_ARTIFACT" "$VERSION" <<'PY'
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

json_path = Path(sys.argv[1])
xml_path = Path(sys.argv[2])
core_pom = Path(sys.argv[3])
compose_pom = Path(sys.argv[4])
group_id, core_artifact, compose_artifact, version = sys.argv[5:9]

data = json.loads(json_path.read_text(encoding="utf-8"))
if data.get("bomFormat") != "CycloneDX":
    raise SystemExit("SBOM is not CycloneDX JSON")
if not data.get("specVersion"):
    raise SystemExit("SBOM specVersion is missing")

xml_root = ET.parse(xml_path).getroot()
if not xml_root.tag.endswith("bom") or "cyclonedx" not in xml_root.tag.lower():
    raise SystemExit("SBOM XML is not a CycloneDX bom")

def pom_coordinates(path: Path) -> tuple[str, str, str]:
    root = ET.parse(path).getroot()
    def value(name: str) -> str:
        element = root.find(f"{{*}}{name}")
        if element is None or not element.text:
            raise SystemExit(f"{path.name} is missing {name}")
        return element.text.strip()
    return value("groupId"), value("artifactId"), value("version")

expected = [
    (core_pom, (group_id, core_artifact, version)),
    (compose_pom, (group_id, compose_artifact, version)),
]
for path, coordinates in expected:
    actual = pom_coordinates(path)
    if actual != coordinates:
        raise SystemExit(f"Unexpected POM coordinates in {path.name}: {actual} != {coordinates}")

print(f"CycloneDX and Maven metadata verified: specVersion={data['specVersion']}")
PY

printf 'Release bundle verification passed for %s:%s and %s:%s\n' \
  "$GROUP_ID" "$CORE_ARTIFACT" "$GROUP_ID" "$COMPOSE_ARTIFACT"
