# ABI compatibility policy

The repository is preparing its first public Maven release. The public API is documented in `docs/PUBLIC-API.md`, and Core uses Kotlin explicit API mode.

After the first release API is finalized and a full Gradle build is available, enable Kotlin Gradle Plugin ABI validation separately in both published modules, generate the reference dumps with `updateKotlinAbi`, review them, and commit them. CI should then run `checkKotlinAbi`.

Do not generate or approve an ABI baseline from an environment that cannot compile the real Android and JVM publications. A source grep or an unverified hand-written dump is not a substitute for the published bytecode contract.

Release rules:

- patch/minor releases must preserve binary compatibility;
- intentional breaking changes require a major version;
- deprecated API remains available for a documented migration window;
- POM dependency-scope changes are reviewed as API changes;
- the independent Maven consumer must compile before release.
