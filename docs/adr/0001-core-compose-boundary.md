# ADR 0001: Separate the pure calendar engine from Compose

## Status

Accepted — 2026-07-29

## Context

Conversion and arithmetic previously lived in the Android library. Although the implementation itself was mostly pure, the artifact still required Android and Compose.

## Decision

Introduce `:calendar-core`, publish it as `persian-calendar-core`, and make the Compose artifact depend on it through `api`.

## Consequences

- Domain-only consumers avoid Android dependencies.
- Conversion has one source of truth.
- The Compose artifact remains source-compatible through an internal facade.
- Two artifacts must be published with the same version.
