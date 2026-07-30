# Professional Architecture Transformation Report

Date: 2026-07-29

## Scope

The project was transformed from a two-module Android showcase/library into a release-oriented library system with a pure calendar engine, a Compose adapter, an unpublished showcase, and independent Compose and Java Maven consumers.

## Resulting module graph

```text
:app -> :calendar -> :calendar-core
samples/maven-consumer -> published persian-date-picker-compose -> published persian-calendar-core
```

No circular dependency was introduced.

## Implemented architecture

### Pure Core boundary

`calendar-core` contains only Kotlin/JVM and `java.time` code. It owns:

- `JalaliDate`
- `JalaliDateRange`
- Gregorian/Jalali conversion
- leap-year and month-length rules
- day arithmetic
- supported-year invariants
- Persian, Arabic-Indic, and Latin digit normalization

Android, AndroidX, Compose, coroutines, dependency injection, resource access, and lifecycle APIs are prohibited by `scripts/verify-architecture.sh`.

### Compose boundary

`calendar` owns UI-only concerns:

- Material 3 rendering
- typed dialog entry points
- localization and RTL/LTR
- accessibility semantics
- state holders and UDF events
- constraints and UI formatting
- compatibility adapters for the existing API

The internal Compose engine delegates all calendar calculations to Core.

### State model

- `SingleDatePickerState` and `DateRangePickerState` are plain `@Stable` holders for committed selection state.
- Committed selection mutations are event-driven and validated before commit; transient month/page/mode state remains local to the dialog.
- Savers store primitive date parts only.
- The state holder owns effective constraints; stateful dialogs copy those rules into the UI config.
- A host application may use a ViewModel, but the library does not require one.

### Compatibility strategy

- Existing `SoleimaniDate` fields and typed callbacks are preserved.
- Results additionally expose `JalaliDate` and `JalaliDateRange`.
- The misspelled legacy theme remains deprecated with a replacement.
- Low-level screen entry points remain deprecated in favor of typed wrappers.

## Publishing architecture

Two coordinated artifacts share one semantic version:

```text
io.github.alischiller:persian-calendar-core:1.0.0
io.github.alischiller:persian-date-picker-compose:1.0.0
```

The Compose POM exports Core transitively. The independent consumer sample verifies the publication rather than relying only on Gradle project dependencies.

## Quality gates added

- standalone Core compile and deterministic conversion smoke test
- architecture/import boundary verification
- obvious secret and runtime-placeholder scan
- Persian/English resource-key and format-placeholder parity
- Gradle Wrapper JAR and distribution checksum validation
- Core and Android unit tests in CI
- Android Lint, Debug and minified Release builds in CI
- Managed Device Compose tests
- independent Compose and Java Maven-local consumer builds
- Dependency Review
- CodeQL for Java/Kotlin
- grouped Dependabot updates
- release tag/version guard
- signed Maven Central workflow
- executable release-bundle integrity checks, CycloneDX SBOM, release checksums, source revision metadata, and GitHub provenance attestations

## Deliberate non-decisions

- No repository, use-case, DI, network, persistence, or ViewModel layer was added because no responsibility requires it.
- The project was not presented as KMP; only the Core boundary is future-friendly.
- Detekt 2.x was not introduced because the available release line is not aligned with the current Kotlin toolchain without an unverified migration. Android Lint, Kotlin compiler warnings, ktlint on new Core code, and repository scripts are used instead.
- Kotlin ABI validation was documented but not enabled without a generated and reviewed baseline. Enabling a check with no verified dump would create a false quality signal.

## Verification executed in this environment

| Check | Status |
|---|---|
| Core source compilation with local `kotlinc` | Passed |
| Core Java API smoke compile/run | Passed |
| Core known reference conversions | Passed |
| Core full supported-range endpoint round trips | Passed — 77,736 |
| Signed supported-year parsing | Passed |
| Architecture boundary scan | Passed |
| Localization key/placeholder parity | Passed |
| Publishing metadata static verification | Passed |
| Workflow YAML parsing | Passed |
| Full Gradle dependency resolution | Blocked |
| Android Debug/Release build | Not Executed |
| Android Lint | Not Executed |
| Compose instrumented tests | Not Executed |
| Maven-local consumer Gradle build | Not Executed |
| Signed Maven Central publication | Not Executed |

## Readiness

**Production candidate / production-oriented — Android CI, runtime, independent-consumer, and first signed release verification required.**
