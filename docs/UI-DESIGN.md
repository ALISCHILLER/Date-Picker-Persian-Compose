# UI Design System

## Purpose

The showcase application presents the reusable Persian date-picker library as a coherent product rather than a collection of technical switches. The redesign keeps the picker APIs and calendar behavior unchanged while improving hierarchy, discoverability and accessibility.

## Visual identity

- Primary: violet `#6D5EF5`
- Accent: teal `#14B8A6`
- Light background: `#F7F7FC`
- Dark background: `#090B16`
- Error: `#D92D20`
- Success: `#12B76A`

The custom launcher icon uses the same violet–teal palette and a calendar/check motif.

## Showcase hierarchy

1. **Hero** — product identity, current date and core capability badges.
2. **Picker workspace** — single-date and date-range cards are the primary actions.
3. **Quick actions** — today and clear remain immediately available.
4. **Live summary** — current selections and the upcoming milestone update without navigation.
5. **Active rules** — enabled constraints are shown as compact status chips.
6. **Customization** — language, number formatting and selection behavior are grouped separately.
7. **Event legend** — event indicator meaning remains visible without entering a picker.

## Picker UI

The reusable dialogs retain their public API and use:

- Gradient header with clear selection context
- Separate month and year selectors
- Fixed 7×6 grid for stable geometry
- Range track with distinct endpoints
- Gregorian supporting labels when enabled
- Responsive action bar that stacks at narrow widths or large text scales
- Explicit selected, disabled, today and event semantics

## Responsive behavior

- Compact horizontal padding below `380dp`
- Content width capped at `1080dp`
- Single-column cards on phones
- Summary and active-rule cards become two columns from `820dp`
- Picker cards become side by side from `620dp`
- Dialog dimensions continue to derive from actual Compose constraints, safe areas and font scale

## Accessibility

- Buttons and cards expose button roles and descriptions
- Disabled repeat action exposes disabled semantics
- Language options expose selected state
- Switch rows are clickable across their full surface
- Text is not truncated to a single line for primary selection values
- Color is supplemented by text, icons and state descriptions

## Screenshot policy

`docs/screenshots/app-showcase.png` is a source-matched promotional composition generated from the current design. It must not be described as a runtime screenshot until the app has been built and captured on an emulator or physical device.
