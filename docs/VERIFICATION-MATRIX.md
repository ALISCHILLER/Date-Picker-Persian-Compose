# Verification matrix

Evidence is split by domain. A successful static check is not reported as a successful Android build or runtime test.

| Area | Status | Evidence / command |
|---|---|---|
| Source review | Statically reviewed | Repository inspection and this transformation report |
| Architecture dependency direction | Passed | `scripts/verify-architecture.sh` |
| Core Android/Compose isolation | Passed | import boundary scan in `scripts/verify-architecture.sh` |
| Gradle Wrapper JAR checksum | Passed | `scripts/verify-gradle-wrapper.sh` |
| Gradle distribution checksum metadata | Passed | pinned `distributionSha256Sum` verified statically |
| Core standalone Kotlin compilation | Passed with installed compiler, JVM target 17 | `scripts/verify-core-standalone.sh`; not a substitute for the blocked Gradle/Kotlin 2.2.20 build |
| Core standalone Java interoperability | Passed | `scripts/verify-core-standalone.sh` |
| Known reference conversions | Passed | standalone verification and Core tests |
| Full supported-range month endpoints | Passed — 77,736 | `scripts/verify-core-standalone.sh` |
| Leap/month/year transitions | Passed | standalone verification |
| Typed parse errors and signed years | Passed | standalone verification and Core tests |
| Range ordering and inclusive invariants | Passed | standalone verification and tests |
| Persian/Arabic/Latin digit normalization | Passed | standalone verification and tests |
| Persian/English key and format-placeholder parity | Passed | `scripts/verify-architecture.sh` |
| Locale declaration (`fa`, `en`) | Passed statically | manifest/XML validation |
| RTL/LTR rendering | Statically reviewed; runtime not executed | resource/config review and existing UI tests |
| Accessibility semantics | Statically reviewed; tests added | pane-title, large-font, action visibility tests |
| Manifest permissions | Passed statically | no requested permissions in the sample app |
| Backup and cleartext policy | Passed statically | manifest and instrumented assertions |
| Obvious hard-coded secret scan | Passed | architecture script; not a substitute for hosted secret scanning |
| AGPL-3.0 text integrity | Passed | `scripts/verify-license.sh` and `LICENSE.sha256` |
| Publication metadata | Passed statically | `scripts/verify-publishing-config.sh` |
| Release bundle collector/verifier | Passed with executable synthetic fixture | required artifacts, CycloneDX, checksums and source-revision metadata |
| Workflow refs, credentials, permissions, cache policy and YAML syntax | Passed | `scripts/verify-workflows.sh` and `scripts/verify-repository.sh` |
| Dependency Review / CodeQL configuration | Statically reviewed | GitHub workflows; hosted runs not executed here |
| SBOM configuration | Passed statically | CycloneDX plugin and release workflow |
| Provenance attestation configuration | Passed statically | release workflow; hosted attestation not executed |
| Gradle dependency resolution | Blocked | DNS failure in `docs/verification/gradle-version-master-complete.txt` |
| Android Debug build | Not executed | Gradle distribution and Android SDK unavailable |
| Android minified Release build | Not executed | Gradle distribution and Android SDK unavailable |
| Android unit tests | Not executed | Gradle resolution unavailable |
| Android Lint | Not executed | Gradle and Android SDK unavailable |
| Compose managed-device tests | Not executed | Android emulator/KVM workflow required |
| Independent Maven consumer Gradle build | Not executed | Gradle resolution and Android SDK unavailable |
| Maven Central signed deployment | Not executed | owner credentials and hosted release workflow required |
| iOS/Desktop/JS/Wasm builds | Not applicable | targets are not configured |
| Runtime behavior on device | Not executed | no emulator/device was available |
| README showcase | Present, source-matched mockup | not represented as an emulator screenshot |

## Classification

**Production candidate / production-oriented — not production-ready until Android CI, runtime smoke testing, independent consumer compilation, and the first signed release succeed.**
