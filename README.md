# LudoMaster — Android

**Brand:** Ludo Masters. The LM shield is the in-app emblem; the supplied full Ludo Masters artwork is the splash and promotional identity.

Independent Android Java project for offline Ludo and optional custom rules. **The offline Classic board prototype is present; production-device validation remains pending.** All game logic and artwork in this repository are independently authored or provided by the project owner.

## Repository separation
- **jighem/ludomaster_android** — Android offline gameplay, local match history, player mapping and future sync client.
- **jighem/Ludomasters** — Existing web game, accounts, league management, leaderboards and future authenticated result-import API.

## Agreed V1 rules
- Offline pass-and-play for 2–4 players.
- Optional combat points: capture +5 and being captured −5.
- Optional immunity: gain one shield per successful capture, with defender choice to spend a shield when attacked; shields currently accumulate without a cap.
- Optional Dynamic Home Shuffle every 5 minutes: swap each color's home yard, starting square and finishing lane; keep physical token positions fixed; forward-only travel (an extra lap if new entrance was passed); finished pawns remain finished; resolve after current move.
- Match-end local-name-to-league-roster mapping with duplicate prevention.
- Eventually submit self-reported offline match results to ludohojaye.com via an authenticated, idempotent API; league-admin approval recommended before official rankings.

## Project state
The initial offline Classic board and tests are present. Combat scoring and immunity are implemented on a feature branch. Dynamic Home Shuffle, sync endpoint, Gradle wrapper and production QA remain outstanding. The app intentionally has no INTERNET permission before a secure API is built.

## Developer prerequisites
Open in Android Studio supporting Android Gradle Plugin 8.7.3, Java 17, and Android SDK 35. A reproducible Gradle wrapper and CI are planned.

See [architecture and roadmap](docs/ARCHITECTURE.md).

## Licensing
A third-party Android reference was inspected for feasibility, but its license could not be established. Do not copy code, audio, board art, UI graphics or network dependencies without authorization.

## Branding assets
The launcher label and main activity use **Ludo Masters** and an LM shield. The vector shield currently included is a stand-in; the supplied high-resolution original logo and splash art must be uploaded as binary PNG assets (see [branding instructions](docs/BRANDING.md)) before release.
