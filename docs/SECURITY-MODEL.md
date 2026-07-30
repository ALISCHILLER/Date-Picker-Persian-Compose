# Security and privacy model

## Scope

The published artifacts are local calendar libraries. They do not perform networking, authentication, storage, analytics, advertising, background work, or cryptographic operations. The sample application exists only to demonstrate the API.

## Trust boundaries

- Consumer-provided dates, ranges, formatters, strings, and constraints are untrusted input.
- Repository workflows, dependency metadata, and release credentials are supply-chain boundaries.
- Android resources and locale configuration are presentation inputs, not business-logic sources of truth.

## Controls

- `calendar-core` validates supported year, month, day, and ordered-range invariants.
- Detailed parsing returns a typed error instead of throwing for normal user-input failures.
- The sample app requests no Android permissions, disables backup, and disables cleartext traffic.
- Maven Central credentials and signing keys are read only from protected GitHub environment secrets.
- Every release generates CycloneDX SBOM files, SHA-256 checksums, source commit/ref metadata, and GitHub artifact attestations.
- Dependency Review, CodeQL, Android Lint, Core ktlint, unit tests, and managed-device tests run in CI.

## Non-goals

The library does not claim to secure host-application data, network traffic, account state, or business authorization. Host applications remain responsible for those controls and for reviewing AGPL-3.0 obligations.

## Reporting

Use GitHub private security advisories. Do not include production secrets, private source code, personal information, or proprietary datasets in a report.
