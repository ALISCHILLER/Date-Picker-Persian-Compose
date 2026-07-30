# ADR 0002: Plain saveable state holders and UDF

## Status

Accepted — 2026-07-29

## Context

A reusable dialog must support state hoisting without forcing a ViewModel, DI container, or lifecycle owner on its host.

## Decision

Expose `SingleDatePickerState` and `DateRangePickerState`, remembered through custom Savers, and mutate them only through typed events.

## Consequences

- State has one owner and is independently testable.
- Hosts can observe selection before confirmation.
- Process recreation can restore primitive state.
- Consumers must create state with constraints equivalent to the dialog configuration.
