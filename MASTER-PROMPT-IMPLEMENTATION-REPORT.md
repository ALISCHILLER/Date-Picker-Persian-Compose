# Android / Compose Master Prompt — Implementation Report

Date: 2026-07-29

Scope: complete hardening of the latest professional project output

License: GNU AGPL-3.0-only, unchanged

## Executive result

The repository is now structured as a publishable two-artifact library system with a pure Kotlin/JVM calendar engine, an Android Compose presentation artifact, an unpublished showcase app, explicit release evidence, and independent-consumer verification in CI.

The implementation deliberately follows dependency direction and observable behavior rather than adding decorative architecture layers. No network, database, repository, use-case, DI, or ViewModel abstraction was introduced because the product has no responsibility that requires them.

## Actual project matrix

```text
:app             Android sample application; never published
:calendar        Android/Jetpack Compose library; publishes AAR
:calendar-core   framework-free Kotlin/JVM engine; publishes JAR
```

Published coordinates:

```text
io.github.alischiller:persian-calendar-core:1.0.0
io.github.alischiller:persian-date-picker-compose:1.0.0
```

No iOS, Desktop, JS, Wasm, KMM, or KMP target is configured or claimed.

## Phase 1 — architecture and public API

### Changes

- Kept all deterministic calendar conversion and arithmetic in `:calendar-core`.
- Preserved one source of truth for conversion, leap-year rules, month length, and date arithmetic.
- Added typed parsing through `JalaliDateParseResult` and `JalaliDateParseError` instead of ambiguous nullable-only feedback.
- Added injected `Clock` overloads for deterministic “today” behavior.
- Strengthened date-range ordering invariants.
- Added explicit state-transition result types for single-date and range selection.
- Kept `SingleDatePickerState` and `DateRangePickerState` as plain saveable state holders without mandatory Lifecycle, ViewModel, DI, or coroutine ownership.
- Kept compatibility APIs while moving low-level implementation composables to `internal`.
- Removed legacy process-global resource initialization from the sample; scoped Compose resource providers are now the demonstrated default while the old API remains only for compatibility.
- Corrected the unpublished sample package typo from `persioncalendar` to `persiancalendar`; Maven coordinates and library package names were not changed.

### Compatibility

Existing `SoleimaniDate` and typed dialog callbacks remain available. New Core types are additive. The sample application package migration affects only installations of the showcase app, not library consumers.

## Phase 2 — Android, localization, accessibility, and privacy

### Changes

- Declared supported app locales through `android:localeConfig` with `fa` and `en`.
- Kept RTL support explicit and retained equal resource-key and format-placeholder coverage between Persian and English.
- Added dialog pane-title semantics for assistive technologies.
- Normalized important interactive targets to at least 48 dp where the touched implementation was below that threshold.
- Added large-font and pane-title Compose tests.
- Disabled application backup and cleartext traffic in the sample manifest.
- Confirmed the sample requests no Android permission, including `INTERNET`.
- Added instrumented assertions for package identity, backup, cleartext, and permission policy.
- Removed unmeasured baseline-profile placeholder files; no performance benefit is claimed without a generated profile and benchmark evidence.

## Phase 3 — code quality and deterministic verification

### Changes

- Core uses explicit API mode, JDK 17, warnings-as-errors, and ktlint.
- Android modules use release-blocking Android Lint with HTML, XML, and SARIF reports.
- Added a repository verification entry point: `./scripts/verify-repository.sh`.
- Added static checks for architecture boundaries, forbidden runtime placeholders, package typo regression, resources, manifest policy, publishing metadata, wrapper integrity, license integrity, and workflow syntax.
- Added direct Java interoperability compilation and an independent Java Maven consumer to protect the published JVM contract.
- Expanded Core verification to every first and final day of every month in the supported algorithm range.

### Executed result

```text
Full supported-range endpoint round trips: 77,736 Passed
Known conversion references: Passed
Leap/month/year transitions: Passed
Typed parsing and signed-year parsing: Passed
Persian/Arabic/Latin digits: Passed
Standalone Kotlin compilation: Passed
Standalone Java interoperability: Passed
```

The standalone smoke suite uses the compiler installed in the analysis environment with JVM target 17. It validates the framework-free source and Java contract, but does not replace the blocked Gradle/Kotlin 2.2.20 build.

## Phase 4 — supply chain and release engineering

### Changes

- Pinned the Gradle distribution SHA-256.
- Added a committed checksum for the current known-good Gradle Wrapper JAR and local verification script.
- Retained official repositories only: Google, Maven Central, and Gradle Plugin Portal where appropriate.
- Added CycloneDX JSON/XML SBOM generation.
- Added Maven-local publication collection into an immutable release-evidence bundle.
- Added per-file checksums, release ZIP checksum, License, Notice, Changelog, and source commit/ref/workflow metadata to the bundle.
- Added GitHub artifact provenance and SBOM attestations.
- Added Dependency Review, dependency graph submission, CodeQL, Dependabot, CODEOWNERS, PR template, issue forms, and private security-reporting guidance.
- Updated hosted CI actions to their current supported major lines and selected Gradle Actions’ basic open-source cache provider explicitly.
- Kept secrets in GitHub environment secrets only; no token, password, signing key, or Central credential is stored in the repository.
- Release publication remains gated by repository checks, version/tag equality, Core tests, Android tests/lint/build, Maven-local publication, independent consumer build, release-bundle verification, signing, and Central deployment.

## Toolchain decision

The stable current stack was not blindly upgraded to a new major generation. AGP/Gradle/Kotlin are retained because a major migration could not be built and rolled back in this restricted environment. Activity Compose was upgraded in isolation, and CycloneDX was added as a build-only release dependency. See `docs/TOOLCHAIN.md`.

## Verification evidence

- `docs/verification/master-static-verification.txt`
- `docs/verification/gradle-version-master-complete.txt`
- `docs/VERIFICATION-MATRIX.md`
- `LICENSE.sha256`

The Gradle wrapper attempted to download Gradle 8.11.1 but DNS resolution for `services.gradle.org` failed. The environment also has no Android SDK or `sdkmanager`. Consequently, Android build, Lint, Gradle tests, managed-device tests, Maven-local consumer compilation, and signed Central deployment are **Not Executed**, not Passed or Failed.

## Remaining release gates

1. Run all three GitHub workflows successfully from a clean clone.
2. Review Android Lint and SARIF results.
3. Run managed-device UI/instrumented tests, including RTL, large text, dark mode, and release-minified smoke flows.
4. Run the independent Maven consumer against the locally published artifacts.
5. Generate and review ABI baselines before making API compatibility a release-blocking gate.
6. Complete the first signed Maven Central release and verify both dependency coordinates from a separate repository.
7. Replace the source-matched README mockup with a real emulator/device screenshot when runtime execution is available.

## Final classification

**Production candidate / production-oriented. Not production-ready until the remaining runtime and release gates have real successful evidence.**
