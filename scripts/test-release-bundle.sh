#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/persian-calendar-release-test.XXXXXX")"
trap 'rm -rf "$WORK_DIR"' EXIT

property() {
  awk -F= -v key="$1" '$1 == key { sub(/^[^=]*=/, ""); print; exit }' \
    "$ROOT_DIR/gradle.properties" | tr -d '\r' | xargs
}

GROUP_ID="$(property LIBRARY_GROUP)"
CORE_ARTIFACT="$(property LIBRARY_CORE_ARTIFACT)"
COMPOSE_ARTIFACT="$(property LIBRARY_COMPOSE_ARTIFACT)"
VERSION="$(property LIBRARY_VERSION)"
GROUP_PATH="${GROUP_ID//./\/}"
MAVEN_REPO="$WORK_DIR/m2"
OUTPUT_DIR="$WORK_DIR/release"

create_publication() {
  local artifact="$1"
  local packaging="$2"
  local target="$MAVEN_REPO/$GROUP_PATH/$artifact/$VERSION"
  local archive="$target/$artifact-$VERSION.$packaging"
  mkdir -p "$target" "$WORK_DIR/archive-content"
  printf 'synthetic %s\n' "$artifact" > "$WORK_DIR/archive-content/README.txt"
  if [[ "$packaging" == "aar" ]]; then
    printf '<manifest package="com.example.synthetic"/>\n' \
      > "$WORK_DIR/archive-content/AndroidManifest.xml"
  fi
  (
    cd "$WORK_DIR/archive-content"
    zip -q -r "$archive" .
  )
  rm -rf "$WORK_DIR/archive-content"

  cat > "$target/$artifact-$VERSION.pom" <<POM
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>$GROUP_ID</groupId>
  <artifactId>$artifact</artifactId>
  <version>$VERSION</version>
  <packaging>$packaging</packaging>
</project>
POM

  mkdir -p "$WORK_DIR/sources" "$WORK_DIR/javadoc"
  printf 'synthetic source\n' > "$WORK_DIR/sources/Source.kt"
  printf 'synthetic documentation\n' > "$WORK_DIR/javadoc/index.html"
  (cd "$WORK_DIR/sources" && zip -q -r "$target/$artifact-$VERSION-sources.jar" .)
  (cd "$WORK_DIR/javadoc" && zip -q -r "$target/$artifact-$VERSION-javadoc.jar" .)
  rm -rf "$WORK_DIR/sources" "$WORK_DIR/javadoc"
}

create_publication "$CORE_ARTIFACT" jar
create_publication "$COMPOSE_ARTIFACT" aar
SBOM_DIR="$WORK_DIR/cyclonedx"
mkdir -p "$SBOM_DIR"
printf '{"bomFormat":"CycloneDX","specVersion":"1.6","components":[]}\n' > "$SBOM_DIR/bom.json"
printf '<?xml version="1.0"?><bom xmlns="http://cyclonedx.org/schema/bom/1.6" version="1"/>\n' > "$SBOM_DIR/bom.xml"

MAVEN_REPO_LOCAL="$MAVEN_REPO" \
RELEASE_OUTPUT_DIR="$OUTPUT_DIR" \
CYCLONEDX_REPORT_DIR="$SBOM_DIR" \
GITHUB_SHA="0123456789abcdef0123456789abcdef01234567" \
GITHUB_REF_NAME="v$VERSION" \
GITHUB_RUN_ID="synthetic" \
  "$ROOT_DIR/scripts/collect-release-artifacts.sh" >/dev/null

RELEASE_OUTPUT_DIR="$OUTPUT_DIR" "$ROOT_DIR/scripts/verify-release-bundle.sh" >/dev/null

grep -Fq 'source_commit=0123456789abcdef0123456789abcdef01234567' \
  "$OUTPUT_DIR/RELEASE-METADATA.txt"
grep -Fq "source_ref=v$VERSION" "$OUTPUT_DIR/RELEASE-METADATA.txt"

printf 'Synthetic release-bundle collection and verification passed.\n'
