# Maven Central Publishing Readiness

## Publications

```text
io.github.alischiller:persian-calendar-core:1.0.0       (JVM JAR)
io.github.alischiller:persian-date-picker-compose:1.0.0 (Android AAR)
```

The Compose publication exports Core as a transitive API dependency. The `:app` module is excluded.

## License

Both POMs declare GNU AGPL-3.0-only. `LICENSE.md` remains unchanged.

## Configured release controls

- complete POM metadata for both artifacts;
- source and documentation artifacts through the publishing plugin;
- in-memory GPG signing;
- Central Portal publication;
- release tag/version equality guard;
- local publication of both modules;
- independent Maven consumer build;
- no credentials stored in the repository;
- CycloneDX JSON/XML SBOM;
- release-evidence ZIP, per-file checksums, License, Notice, Changelog, source commit/ref metadata, and GitHub provenance attestations.

## External setup still required

- verify `io.github.alischiller` in Central Portal;
- generate a Central user token;
- publish the GPG public key;
- configure the four GitHub Secrets;
- run and review the first tagged release workflow.

## Status

Static publication verification: **Passed**.

Real Gradle publication and Central upload: **Not Executed in this environment** because the Gradle distribution, Android SDK, and external dependency resolution were unavailable.
