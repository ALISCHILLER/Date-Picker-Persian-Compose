#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPERTIES="$ROOT_DIR/gradle.properties"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"
OUTPUT_DIR="${RELEASE_OUTPUT_DIR:-$ROOT_DIR/release-artifacts}"
CYCLONEDX_REPORT_DIR="${CYCLONEDX_REPORT_DIR:-$ROOT_DIR/build/reports/cyclonedx}"

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
BUNDLE_NAME="persian-date-picker-compose-${VERSION}-release"

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/maven/$GROUP_PATH" "$OUTPUT_DIR/sbom"

copy_publication() {
  local artifact="$1"
  local source="$MAVEN_REPO_LOCAL/$GROUP_PATH/$artifact/$VERSION"
  local target="$OUTPUT_DIR/maven/$GROUP_PATH/$artifact/$VERSION"
  if [[ ! -d "$source" ]]; then
    echo "Missing Maven-local publication: $source" >&2
    exit 1
  fi
  mkdir -p "$(dirname "$target")"
  cp -R "$source" "$target"
}

copy_publication "$CORE_ARTIFACT"
copy_publication "$COMPOSE_ARTIFACT"

for extension in json xml; do
  source="$CYCLONEDX_REPORT_DIR/bom.$extension"
  if [[ ! -s "$source" ]]; then
    echo "Missing CycloneDX SBOM: $source" >&2
    exit 1
  fi
  cp "$source" "$OUTPUT_DIR/sbom/bom.$extension"
done

cp "$ROOT_DIR/LICENSE.md" "$OUTPUT_DIR/LICENSE.md"
cp "$ROOT_DIR/NOTICE.md" "$OUTPUT_DIR/NOTICE.md"
cp "$ROOT_DIR/CHANGELOG.md" "$OUTPUT_DIR/CHANGELOG.md"

cat > "$OUTPUT_DIR/RELEASE-METADATA.txt" <<EOF
repository=https://github.com/ALISCHILLER/Date-Picker-Persian-Compose
source_commit=${GITHUB_SHA:-unknown}
source_ref=${GITHUB_REF_NAME:-unknown}
workflow_run_id=${GITHUB_RUN_ID:-unknown}
group_id=$GROUP_ID
core_artifact=$CORE_ARTIFACT
compose_artifact=$COMPOSE_ARTIFACT
version=$VERSION
EOF

(
  cd "$OUTPUT_DIR"
  find maven sbom -type f -print0 | sort -z | xargs -0 sha256sum > SHA256SUMS
  sha256sum LICENSE.md NOTICE.md CHANGELOG.md RELEASE-METADATA.txt >> SHA256SUMS
  zip -q -r "$BUNDLE_NAME.zip" \
    maven sbom LICENSE.md NOTICE.md CHANGELOG.md RELEASE-METADATA.txt SHA256SUMS
  sha256sum "$BUNDLE_NAME.zip" > "$BUNDLE_NAME.zip.sha256"
)

printf 'Release evidence prepared in %s\n' "$OUTPUT_DIR"
printf 'Bundle: %s\n' "$OUTPUT_DIR/$BUNDLE_NAME.zip"
