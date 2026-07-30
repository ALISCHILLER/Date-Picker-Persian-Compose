## Summary

Describe the user-visible or API-visible change and why it is needed.

## Risk and compatibility

- [ ] Public API compatibility was considered.
- [ ] Maven coordinates and versioning remain correct.
- [ ] Persian/English and RTL/LTR behavior were considered.
- [ ] Security, permissions, backup, cleartext, and secrets were considered.
- [ ] The change is minimal, reviewable, and reversible.

## Verification

- [ ] `./scripts/verify-repository.sh`
- [ ] `./gradlew ktlintCheck :calendar-core:check`
- [ ] Android unit tests and lint
- [ ] Compose/instrumented tests when UI behavior changed
- [ ] Independent Maven consumer when public API or publication changed

List any command that was not executed and explain why. Do not mark an unexecuted check as passed.
