# Toolchain decision record

Date reviewed: 2026-07-29

## Active targets

| Target | Status | Module |
|---|---|---|
| Android application | Active sample only | `:app` |
| Android library | Active and publishable | `:calendar` |
| Kotlin/JVM library | Active and publishable | `:calendar-core` |
| iOS | Not configured | — |
| Desktop | Not configured | — |
| JS/Wasm | Not configured | — |
| Kotlin Multiplatform | Not configured | — |

The repository does not advertise inactive platforms. The framework-free Core boundary is a prerequisite for a future KMP migration, not evidence that KMP already exists.

## Version matrix

| Component | Selected | Decision | Verification |
|---|---:|---|---|
| JDK | 17 | Retain | Required by the selected Android toolchain; configured in modules and CI |
| Gradle | 8.11.1 | Retain | Distribution SHA-256 is pinned; runtime download was blocked in this environment |
| Android Gradle Plugin | 8.10.1 | Retain | Compatible with API 36 and Gradle 8.11.1; avoid an unverified major migration |
| Kotlin | 2.2.20 | Retain | Stable compiler/plugin line already used by all modules |
| Compose BOM | 2026.06.00 | Retain | Centralized stable Compose alignment |
| Activity Compose | 1.13.0 | Upgrade | Low-scope stable AndroidX update |
| Maven Publish plugin | 0.34.0 | Retain | Compatible with the current Gradle/AGP generation |
| CycloneDX Gradle plugin | 3.3.0 | Add | Generates JSON and XML SBOM release evidence |
| ktlint engine | 1.8.0 | Retain for Core | Core is the strict portable API boundary; Android Lint covers Android modules |
| GitHub hosted actions | Checkout 6 / Setup Java 5 / Gradle Actions 6 / Upload Artifact 6 / CodeQL 4 / Attest 4 | Upgrade | Current supported major lines; Gradle Actions uses `cache-provider: basic` |

## Deliberate deferrals

- AGP 9.x is a major build migration and was not applied without a clean dependency resolution, Android build, and rollback-tested branch.
- No preview, RC, EAP, snapshot, dynamic, or wildcard dependency version was introduced.
- Dependency verification metadata and lock files were not fabricated. They must be generated from a successful trusted Gradle resolution and reviewed before commit.
- KMP targets were not invented. A future migration needs a separate compatibility plan for `java.time`, binary/API compatibility, and target-specific testing.

## Rollback

The Activity Compose update can be reverted independently in `gradle/libs.versions.toml`. CycloneDX can be removed by reverting the root plugin and the two workflow tasks; it does not alter runtime code.
