# Architecture and phased implementation

## Separate systems
Android: standalone clean-room Java game with a pure, deterministic rules engine and original board art. Web: existing Ludomasters server and database, with a new result-import API added in a *separate PR* only once Android submission requirements are agreed.

## Proposed Android modules
- **game/**: immutable game state, turn resolver, dice, captures, shields, scoring, dynamic home mappings.
- **board/**: original 15×15 responsive Android board, tokens, home boxes, track animation.
- **storage/**: persist match state, completed results, offline sync queue (Room/SQLite).
- **results/**: final positions, optional combat tally, player identity mapping.
- **sync/**: eventual HTTPS authenticated API, retries and UUID idempotency, no passwords embedded in APK.

## Dynamic Home Shuffle invariants
- 52-square shared circular path; a pawn's physical track index does not move during shuffle.
- New color-to-quadrant permutation changes start squares and final entrance/finishing lanes, not pawn owner.
- A pawn that passed its new home entrance continues forward to it on its next lap.
- Apply pending five-minute shuffle only after current move/capture resolves; preserve turn.
- Completed pawns stay completed; persist shuffle event history.
- Final V1 rule: pawns already inside a finishing lane move together with their colour's finishing lane to its new quadrant, retaining their lane depth (progress 52–56); completed pawns remain completed. Shared-track pawns stay physically fixed and reach the new entrance via forward-only movement.
- Shuffle RNG and game clock need deterministic tests, including background/resume time behavior.

## Match identity and backend (future proposal, not implemented)
POST /api/mobile/offline-matches with clientMatchId UUID, leagueId, ruleVersion, match timestamps, participant slots, local names, optional mapped player IDs, positions, kills/deaths, scores and shuffle events. Reject duplicate IDs, unauthorized leagues, duplicate mappings and inconsistent totals. Mark self-reported results pending review for official competitive standings. Never claim anti-cheat guarantees for an offline game client.

## Delivery phases
1. Foundation (this PR): Android app shell, core-rule prototypes, documentation.
2. Playable Ludo: board, state machine, local multiplayer and deterministic tests.
3. Optional game rules, timers and edge cases.
4. Local persistence, result review, mapping and queued sync.
5. Web backend import endpoint through separate PR in Ludomasters.
6. Authenticated Android sync, device QA, reproducible builds, signed APK.

## Copyright
Only use original or properly licensed assets/code. Do not incorporate third-party game art or source without a license or permission.
