# Release checklist

## Before the tag

- [ ] Review public API and migration impact.
- [ ] Update `LIBRARY_VERSION`, changelog, README, and migration notes.
- [ ] Run `./scripts/verify-repository.sh`.
- [ ] Run `./gradlew :calendar-core:ktlintCheck :calendar-core:check :calendar:testDebugUnitTest`.
- [ ] Run Android lint, debug/release builds, and managed-device tests.
- [ ] Run `./scripts/verify-maven-consumer.sh`.
- [ ] Review dependency changes, licenses, and generated CycloneDX SBOM.
- [ ] Confirm no key, token, keystore, or signing material is tracked.

## Release

- [ ] Create signed/annotated tag `vX.Y.Z` matching `LIBRARY_VERSION`.
- [ ] Publish a GitHub Release from the immutable tag.
- [ ] Require the protected `maven-central` environment for deployment secrets.
- [ ] Confirm Central validation and publication succeeded for both artifacts.

## Evidence

- [ ] GitHub Release contains the release ZIP, checksum, `SHA256SUMS`, `RELEASE-METADATA.txt`, and SBOM JSON/XML.
- [ ] GitHub provenance and SBOM attestations are visible for the release bundle.
- [ ] Workflow logs identify the commit and tag.
- [ ] A clean consumer project resolves both Maven Central artifacts.

## Rollback

Maven Central versions are immutable. Never replace a released version. Revoke or deprecate a compromised release, document the reason, rotate credentials when needed, and publish a new patch version.
