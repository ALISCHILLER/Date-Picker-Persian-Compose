# Publishing both libraries to Maven Central

The repository publishes two artifacts with one version:

```text
io.github.alischiller:persian-calendar-core:1.0.0
io.github.alischiller:persian-date-picker-compose:1.0.0
```

`persian-date-picker-compose` declares `persian-calendar-core` as an API dependency, so Compose consumers add only the Compose coordinate. The showcase `:app` is never published.

## Version source of truth

```properties
LIBRARY_GROUP=io.github.alischiller
LIBRARY_CORE_ARTIFACT=persian-calendar-core
LIBRARY_COMPOSE_ARTIFACT=persian-date-picker-compose
LIBRARY_VERSION=1.0.0
```

The same version is intentionally used for both artifacts to keep the compatibility matrix simple.

## License

Both POM files declare **GNU AGPL-3.0-only**, and the full unchanged license text remains in `LICENSE.md`. Maven publication does not require changing the license.

## One-time Central Portal setup

1. Sign in with the `ALISCHILLER` GitHub account.
2. Verify `io.github.alischiller`.
3. Generate a Central Portal user token.
4. Create a GPG signing key and publish its public key.
5. Add these GitHub repository secrets:

```text
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
SIGNING_KEY
SIGNING_PASSWORD
```

Never commit a token, password, private key, `secring.gpg`, or user-level `gradle.properties` file.

## Local verification

Run the aggregate repository policy and standalone checks first:

```bash
./scripts/verify-repository.sh
```

Then run the Gradle release checks in an Android environment:

```bash
./gradlew --no-daemon \
  :calendar-core:ktlintCheck \
  cyclonedxBom \
  :calendar-core:check \
  :calendar:testDebugUnitTest \
  :calendar:lintDebug \
  :calendar:assembleRelease \
  :calendar-core:publishToMavenLocal \
  :calendar:publishToMavenLocal \
  -PskipSigning=true
```

Verify the publication from a project that does not use `project(":calendar")`:

```bash
./scripts/verify-maven-consumer.sh
```

Expected local repositories:

```text
~/.m2/repository/io/github/alischiller/persian-calendar-core/1.0.0/
~/.m2/repository/io/github/alischiller/persian-date-picker-compose/1.0.0/
```

## Release evidence

After Maven-local publication and `cyclonedxBom`, prepare the exact evidence bundle used by CI:

```bash
./scripts/collect-release-artifacts.sh
./scripts/verify-release-bundle.sh
```

The bundle contains both Maven publications, CycloneDX JSON/XML, the unchanged AGPL license, `NOTICE.md`, changelog, `RELEASE-METADATA.txt` with the source commit/ref and workflow run, and SHA-256 checksums. The GitHub Release workflow creates provenance and SBOM attestations for this bundle after Central publication succeeds.

Protect the `maven-central` GitHub environment, restrict who can approve deployment, and store the four publication secrets only in that environment.

## Release procedure

1. Update `LIBRARY_VERSION`.
2. Update `CHANGELOG.md`.
3. Run the verification commands above.
4. Review public API changes and migration impact.
5. Commit the release changes.
6. Create a signed matching tag:

```bash
git tag -s v1.0.0 -m "Persian Calendar libraries 1.0.0"
git push origin v1.0.0
```

7. Create a GitHub Release from that tag.
8. `.github/workflows/publish-maven-central.yml` checks the tag, runs tests/lint/format checks, generates the SBOM, verifies an independent consumer, prepares release evidence, and executes:

```bash
./gradlew --no-daemon publishAndReleaseToMavenCentral
```

Published Maven Central versions are immutable. Fixes require a new version such as `1.0.1`.

## Manual emergency publication

```bash
export ORG_GRADLE_PROJECT_mavenCentralUsername="TOKEN_USERNAME"
export ORG_GRADLE_PROJECT_mavenCentralPassword="TOKEN_PASSWORD"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --export-secret-keys --armor KEY_ID)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="KEY_PASSWORD"

./gradlew --no-daemon publishAndReleaseToMavenCentral
```

Clear these variables from the shell afterward.

## Consumer coordinates

Compose:

```kotlin
implementation("io.github.alischiller:persian-date-picker-compose:1.0.0")
```

Core-only:

```kotlin
implementation("io.github.alischiller:persian-calendar-core:1.0.0")
```

## Provenance verification

After publication, verify the downloaded release bundle with GitHub CLI and its checksum:

```bash
gh attestation verify persian-date-picker-compose-1.0.0-release.zip \
  --repo ALISCHILLER/Date-Picker-Persian-Compose
sha256sum --check persian-date-picker-compose-1.0.0-release.zip.sha256
```

A workflow file existing in the repository is not proof of a successful release. Treat publication as verified only after the Central deployment, consumer resolution, checksums, and attestations are all successful for the tagged commit.
