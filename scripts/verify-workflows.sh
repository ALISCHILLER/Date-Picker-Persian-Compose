#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKFLOW_DIR="$ROOT_DIR/.github/workflows"
ANDROID="$WORKFLOW_DIR/android.yml"
CODEQL="$WORKFLOW_DIR/codeql.yml"
RELEASE="$WORKFLOW_DIR/publish-maven-central.yml"

fail() {
  printf 'Workflow policy verification failed: %s\n' "$1" >&2
  exit 1
}

if grep -RInE 'uses:[[:space:]]+[^[:space:]]+@(main|master|HEAD)([[:space:]]|$)' "$WORKFLOW_DIR"; then
  fail 'floating action branch reference found.'
fi

required_refs=(
  'actions/checkout@v6'
  'actions/setup-java@v5.6.0'
  'actions/upload-artifact@v6.0.0'
  'gradle/actions/setup-gradle@v6'
  'gradle/actions/wrapper-validation@v6'
  'gradle/actions/dependency-submission@v6'
  'actions/dependency-review-action@v4.9.0'
  'github/codeql-action/init@v4'
  'github/codeql-action/analyze@v4'
  'actions/attest@v4'
)
for ref in "${required_refs[@]}"; do
  grep -RqsF "uses: $ref" "$WORKFLOW_DIR" || fail "required action reference is missing: $ref"
done

checkout_count="$(grep -Rh 'uses: actions/checkout@' "$WORKFLOW_DIR" | wc -l | tr -d ' ')"
persist_false_count="$(grep -Rh 'persist-credentials: false' "$WORKFLOW_DIR" | wc -l | tr -d ' ')"
[[ "$checkout_count" == "$persist_false_count" ]] || {
  fail "every checkout must disable persisted credentials (checkout=$checkout_count, hardened=$persist_false_count)."
}

setup_gradle_count="$(grep -Rh 'uses: gradle/actions/setup-gradle@v6' "$WORKFLOW_DIR" | wc -l | tr -d ' ')"
basic_cache_count="$(grep -Rh 'cache-provider: basic' "$WORKFLOW_DIR" | wc -l | tr -d ' ')"
[[ "$setup_gradle_count" == "$basic_cache_count" ]] || {
  fail "every setup-gradle step must select cache-provider: basic."
}

for workflow in "$ANDROID" "$CODEQL" "$RELEASE"; do
  grep -q '^permissions:' "$workflow" || fail "top-level permissions are missing in ${workflow##*/}."
done

grep -q '^  id-token: write$' "$RELEASE" || fail 'release workflow lacks id-token: write.'
grep -q '^  attestations: write$' "$RELEASE" || fail 'release workflow lacks attestations: write.'
grep -q '^  artifact-metadata: write$' "$RELEASE" || fail 'release workflow lacks artifact-metadata: write.'
grep -q '^    environment: maven-central$' "$RELEASE" || fail 'release job is not protected by the maven-central environment.'

grep -q 'cache-read-only: true' "$RELEASE" || fail 'release workflow cache must remain read-only.'

grep -q 'persist-credentials: false' "$RELEASE" || fail 'release checkout must not persist credentials.'

printf 'GitHub workflow action, credential, cache, and permission policy verified.\n'
