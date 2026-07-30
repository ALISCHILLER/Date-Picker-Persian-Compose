# ADR 0004: Release evidence and supply-chain controls

## Status

Accepted — 2026-07-29

## Context

A Maven Central publication must be traceable to a reviewed source revision. A successful build alone does not prove artifact identity, dependency composition, or provenance.

## Decision

- Generate an aggregate CycloneDX JSON/XML SBOM during CI and release.
- Publish both artifacts to Maven Local and verify an independent consumer before Central deployment.
- Package Maven-local publications, SBOM, license, notice, changelog, source commit/ref metadata, and SHA-256 checksums into one immutable evidence bundle.
- Create GitHub provenance and SBOM attestations for the bundle using OIDC.
- Upload evidence only after Maven Central publication succeeds.
- Keep signing keys and Central tokens in the protected `maven-central` GitHub environment.

## Consequences

Release workflows are slightly longer and depend on GitHub attestation availability. In exchange, every version has a verifiable identity, component inventory, and rollback record. No claim of successful release is made until the workflow and Central deployment complete.
