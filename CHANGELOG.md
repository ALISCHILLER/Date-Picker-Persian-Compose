# Changelog

All notable changes to this project should be documented in this file.

## Unreleased

### Added

- Typed Jalali parsing results with stable validation error codes.
- Clock-injected `today` APIs for deterministic tests.
- Explicit state-transition results for single-date and range state holders.
- CycloneDX SBOM generation, SHA-256 release bundles, source commit/ref metadata, and GitHub provenance/SBOM attestations.
- Security model, release checklist, supply-chain ADR, CODEOWNERS, PR checklist, and structured issue forms.
- Application locale declaration and security tests for permissions, backup, and cleartext traffic.
- Split the deterministic Jalali engine into the Android-free `:calendar-core` module and Maven artifact `persian-calendar-core`.
- Added `JalaliDate`, `JalaliDateRange`, `JalaliCalendar`, `CalendarDigits`, and explicit supported-year limits.
- Added saveable single-date and range state holders with UDF events, Core model bridges, default constructors and Java-friendly overloads.
- Added independent Compose and Java Core Maven consumers that verify transitive metadata and published APIs.
- Added architecture and workflow-policy scripts, an executable synthetic release-bundle test, ADRs, public API documentation, and contribution rules.
- Added Dependency Review, CodeQL, and grouped Dependabot configuration.
- Updated hosted CI to Checkout 6, Setup Java 5, Gradle Actions 6, Upload Artifact 6 and Dependency Review 4.9, with the open-source Gradle cache provider selected explicitly.
- Maven Central publication configuration for both `persian-calendar-core` and `persian-date-picker-compose`.
- Signed release workflow triggered by published GitHub Releases.
- Release tag/version guard and local Maven publication verification.
- `PUBLISHING.md` and bilingual dependency-consumption documentation.
- Professional product-style showcase dashboard with hero, picker workspace, live selection summary, active-rule chips and grouped settings.
- Responsive one-column and wide two-column showcase layouts.
- Custom violet–teal launcher icon and unified visual identity.
- Source-matched clickable README showcase image under `docs/screenshots/app-showcase.png`.
- Showcase Compose UI tests for primary picker actions and language choices.
- UI design-system documentation under `docs/UI-DESIGN.md`.
- Persistent showcase language selection for System, Persian and English modes.
- Unit-test coverage for locale initialization, persistence callbacks and invalid stored values.
- Bilingual Persian/English README.
- Source audit and verification matrix under `docs/verification/`.

### Changed

- Opted `:calendar-core` into consistent data-class copy visibility so private constructors are not exposed by generated `copy()` methods under Kotlin 2.2+ and `-Werror`.
- Updated the stable Activity Compose dependency from `1.12.4` to `1.13.0`.
- Android lint is release-blocking and emits HTML, XML, and SARIF reports.
- Corrected the unpublished sample application package from `com.msa.persioncalendar` to `com.msa.persiancalendar`.
- License/notice metadata is merged instead of excluded from packaging.
- CI now runs aggregate repository policy checks, ktlint, and SBOM generation.
- Release deployment is protected by the `maven-central` environment and uploads verifiable evidence.
- The Compose calendar engine now delegates to one Core implementation instead of maintaining a separate algorithm.
- Stateful dialog overloads use the state holder constraints as the validation source of truth.
- The showcase now demonstrates scoped resource providers instead of initializing the legacy process-global resource bridge.
- Selection results now expose Android-free Core dates while preserving the existing compatibility fields.
- Refined the library palette to a consistent violet `#6D5EF5` and teal `#14B8A6` identity.
- Reorganized showcase controls around user tasks instead of a long technical settings page.
- Library dependencies that appear in public API signatures are now exported with Gradle `api` metadata.
- Dependency repositories are limited to official Google, Maven Central and Gradle Plugin Portal endpoints.
- Showcase wording now says “production-oriented” instead of the unverified “production-ready”.

### Removed

- Unmeasured baseline-profile placeholders and the unused Profile Installer dependency.

### Verification

- Full supported-range endpoint verification completed 77,736 Jalali/Gregorian round trips.
- Known conversion references, leap/month/year transitions, signed-year parsing and digit normalization passed.
- Standalone Kotlin/JVM compilation and Java 17 interoperability passed.
- Architecture boundaries, XML/YAML parsing, Persian/English key and format-placeholder parity, publishing metadata, documentation links and synthetic release-bundle verification passed.
- Full Compose/Android compilation remains blocked by the unavailable Gradle distribution and Android SDK in the analysis environment.
- Android builds, Lint, managed-device tests, Maven-local consumer Gradle builds and signed Maven Central publication therefore remain Not Executed.
