# ADR 0003: Verify the published contract with an external consumer

## Status

Accepted — 2026-07-29

## Context

A source-module build can pass even when the generated POM omits a required transitive dependency or the published AAR/JAR exposes an unusable API.

## Decision

Publish Core and Compose to `mavenLocal()` in CI, then build `samples/maven-consumer` using Maven coordinates only.

## Consequences

- Publication metadata is exercised before Maven Central release.
- The smoke test is slower than a normal module build.
- The sample must remain independent from root project dependencies.
