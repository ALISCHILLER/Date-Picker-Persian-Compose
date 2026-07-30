#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="$(sed -n 's/^LIBRARY_VERSION=//p' "$ROOT_DIR/gradle.properties")"

"$ROOT_DIR/gradlew" \
  :calendar-core:publishToMavenLocal \
  :calendar:publishToMavenLocal \
  -PskipSigning=true

"$ROOT_DIR/gradlew" \
  -p "$ROOT_DIR/samples/maven-consumer" \
  :app:assembleDebug \
  :core-consumer:build \
  -PLIBRARY_VERSION="$VERSION"
