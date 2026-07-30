#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WRAPPER_DIR="$ROOT_DIR/gradle/wrapper"
PROPERTIES="$WRAPPER_DIR/gradle-wrapper.properties"

(
  cd "$WRAPPER_DIR"
  sha256sum --check gradle-wrapper.jar.sha256
)

grep -Fq 'distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip' "$PROPERTIES" || {
  echo 'Unexpected Gradle distribution URL.' >&2
  exit 1
}

grep -Fq 'distributionSha256Sum=f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6' "$PROPERTIES" || {
  echo 'Unexpected or missing Gradle 8.11.1 distribution checksum.' >&2
  exit 1
}

printf 'Gradle wrapper JAR and distribution metadata checksums verified.\n'
