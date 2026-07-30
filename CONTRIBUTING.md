# Contributing

## Development principles

- Keep `calendar-core` free of Android and Compose.
- Prefer small reversible changes.
- Do not add architecture layers without a real responsibility.
- Preserve public API and observable behavior unless the change is explicitly breaking.
- Add regression tests for calendar arithmetic, constraints, state, RTL/LTR, and accessibility changes.
- Never commit credentials, signing keys, `local.properties`, generated build outputs, or private screenshots.

## Required checks

```bash
./scripts/verify-repository.sh
./gradlew ktlintCheck :calendar-core:check
./gradlew :calendar:testDebugUnitTest :app:testDebugUnitTest
./gradlew :calendar:lintDebug :app:lintDebug
./gradlew :calendar:assembleRelease :app:assembleRelease
./scripts/verify-maven-consumer.sh
```

Instrumented UI changes should also run:

```bash
./gradlew :app:pixel2Api30DebugAndroidTest :calendar:pixel2Api30DebugAndroidTest
```

## Pull requests

A PR should explain:

1. The problem and evidence.
2. The smallest chosen solution.
3. Public API and behavior impact.
4. Android and Core impact.
5. Verification performed.
6. Rollback approach for risky changes.

## Formatting

New Core code is checked with ktlint. Android modules follow Kotlin official formatting and Android Lint; broad mechanical formatting of unrelated files should not be mixed with behavior changes.

## Public API and release changes

Changes to public models, state holders, Maven metadata, constraints, parser behavior, or Compose entry points must include compatibility analysis and migration notes. Do not generate or commit an ABI baseline until it has been produced by a successful Gradle build and reviewed as the intended public contract.

Measured performance work must include the benchmark setup and before/after evidence. Do not add guessed baseline-profile entries.
