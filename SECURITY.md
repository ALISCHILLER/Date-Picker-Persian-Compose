# Security Policy

## Supported versions

The repository does not yet publish versioned Maven artifacts. Security fixes currently apply to the latest supported source on the default branch.

## Reporting a vulnerability

Do not include credentials, private user data, exploit payloads or other sensitive details in a public issue.

Use GitHub's private vulnerability reporting or security-advisory flow when it is available for this repository. When no private channel is available, open a minimal public issue requesting a private contact channel without disclosing the vulnerability details.

For the detailed threat and trust-boundary model, see [`docs/SECURITY-MODEL.md`](docs/SECURITY-MODEL.md).

## Project security boundaries

- The calendar library does not perform networking or persist user data.
- The showcase stores only the selected interface-language option in private application preferences.
- Release signing credentials, API keys and other secrets must remain outside the repository and CI logs.
- A successful compile is not sufficient release verification; the minified artifact must be smoke-tested.

## Release and supply-chain evidence

Release credentials are scoped to the protected `maven-central` GitHub environment. The release workflow generates CycloneDX SBOM files, SHA-256 checksums, source commit/ref metadata, and GitHub provenance/SBOM attestations. Published Maven Central versions are immutable; a security fix is released under a new version rather than replacing an existing artifact.
