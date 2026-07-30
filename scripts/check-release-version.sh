#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPERTIES_FILE="$ROOT_DIR/gradle.properties"

version="$(awk -F= '$1 == "LIBRARY_VERSION" { print $2 }' "$PROPERTIES_FILE" | tr -d '\r[:space:]')"
if [[ -z "$version" ]]; then
  echo "LIBRARY_VERSION is missing from gradle.properties" >&2
  exit 1
fi

if [[ $# -eq 0 ]]; then
  echo "$version"
  exit 0
fi

tag="${1#refs/tags/}"
tag="${tag#v}"

if [[ "$tag" != "$version" ]]; then
  echo "Release tag/version mismatch: tag=$tag, LIBRARY_VERSION=$version" >&2
  exit 1
fi

echo "Release tag matches LIBRARY_VERSION: $version"
