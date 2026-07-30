#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPERTIES="$ROOT_DIR/gradle.properties"
CATALOG="$ROOT_DIR/gradle/libs.versions.toml"
ROOT_BUILD="$ROOT_DIR/build.gradle.kts"
CORE_BUILD="$ROOT_DIR/calendar-core/build.gradle.kts"
COMPOSE_BUILD="$ROOT_DIR/calendar/build.gradle.kts"
WORKFLOW="$ROOT_DIR/.github/workflows/publish-maven-central.yml"
LICENSE_FILE="$ROOT_DIR/LICENSE.md"

read_property() {
  local key="$1"
  awk -F= -v key="$key" '$1 == key { sub(/^[^=]*=/, ""); print; exit }' "$PROPERTIES" \
    | tr -d '\r' \
    | xargs
}

group_id="$(read_property LIBRARY_GROUP)"
core_artifact="$(read_property LIBRARY_CORE_ARTIFACT)"
compose_artifact="$(read_property LIBRARY_COMPOSE_ARTIFACT)"
version="$(read_property LIBRARY_VERSION)"

[[ "$group_id" == "io.github.alischiller" ]] || { echo "Unexpected LIBRARY_GROUP: $group_id" >&2; exit 1; }
[[ "$core_artifact" == "persian-calendar-core" ]] || { echo "Unexpected core artifact: $core_artifact" >&2; exit 1; }
[[ "$compose_artifact" == "persian-date-picker-compose" ]] || { echo "Unexpected Compose artifact: $compose_artifact" >&2; exit 1; }
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z.-]+)?$ ]] || {
  echo "LIBRARY_VERSION is not a supported semantic version: $version" >&2
  exit 1
}

grep -q 'com.vanniktech.maven.publish' "$CATALOG"
grep -q 'org.cyclonedx.bom' "$CATALOG"
grep -q 'alias(libs.plugins.cyclonedx)' "$ROOT_BUILD"
for build_file in "$CORE_BUILD" "$COMPOSE_BUILD"; do
  grep -q 'publishToMavenCentral()' "$build_file"
  grep -q 'signAllPublications()' "$build_file"
  grep -q 'GNU Affero General Public License v3.0 only' "$build_file"
done

grep -q 'api(project(":calendar-core"))' "$COMPOSE_BUILD"
grep -q 'publishAndReleaseToMavenCentral' "$WORKFLOW"
grep -q ':calendar-core:publishToMavenLocal' "$WORKFLOW"
grep -q ':calendar:publishToMavenLocal' "$WORKFLOW"
grep -q 'cyclonedxBom' "$WORKFLOW"
grep -q 'actions/attest@v4' "$WORKFLOW"
grep -q 'collect-release-artifacts.sh' "$WORKFLOW"
grep -q 'RELEASE-METADATA.txt' "$WORKFLOW"
grep -q 'GNU AFFERO GENERAL PUBLIC LICENSE' "$LICENSE_FILE"

for secret in MAVEN_CENTRAL_USERNAME MAVEN_CENTRAL_PASSWORD SIGNING_KEY SIGNING_PASSWORD; do
  grep -q "secrets\.${secret}" "$WORKFLOW" || {
    echo "Workflow is missing GitHub secret: $secret" >&2
    exit 1
  }
done

"$ROOT_DIR/scripts/verify-license.sh" >/dev/null
"$ROOT_DIR/scripts/verify-gradle-wrapper.sh" >/dev/null
"$ROOT_DIR/scripts/check-release-version.sh" "v$version" >/dev/null

printf 'Publishing configuration verified:\n'
printf '  %s:%s:%s\n' "$group_id" "$core_artifact" "$version"
printf '  %s:%s:%s\n' "$group_id" "$compose_artifact" "$version"
