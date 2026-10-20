# Ludo Ho Jaye — UI Rebrand (Implementation Branch)

Brand: **Ludo Ho Jaye!** — **Dosti Bhi, Takkar Bhi!**

## Approved visual system
- Modern colorful styling; four-token circular emblem; rounded bold wordmark.
- Splash: sky-blue, token bounce and fading text.
- Home: colorful Ludo-board motif, Computer and Pass & Play vertically; separate Profile at bottom.
- Setup: scrollable with compact player rows; Combat, Shield and Shuffle toggles visible.
- Board: 15×15 validated geometry and white raised 3D frame. Keep all four **player-specific** dice, each using red/green/blue/yellow glossy faces with white pips. Two compact player cards above the board, two below; preserve K/D/Shield/Pts.
- Winner: trophy/confetti, all standings, persistent upload and Play Again/Done.
- League: native login, profile, searchable league cards, external website registration.
- Settings: audio, about, persisted defaults; bottom-sheet pause; explicit exit confirmation.
- Audio: original playful sound pack, preserve preference defaults.

## Protect production gameplay
Do not modify MainActivity's path/tile positions, 52-cell track, home entrances, token coordinates or stacking, rule and scoring logic, shuffle timing and recoloring, session UUIDs, auth, upload endpoints or approvals.

## Rollout sequence
1. Create new assets under `lhj_` names; do not overwrite old artwork.
2. Wire splash and menu as functional Android controls, no fixed-coordinate hotspots.
3. Build exact-coordinate board texture separately from white 3D surround.
4. Adapt four existing dice controls, maintaining player-to-dice bindings.
5. Validate 2/3/4 player modes, shuffle, captures, shields, finished tokens, saved uploads, statuses.
6. Share debug APK, obtain visual approval, and only then merge.

## Current branch progress (October 19, 2026)
- Native Ludo Ho Jaye name, four-pawn vector emblem, pale-blue splash, animated colored loader.
- Home screen uses responsive native Computer / Pass & Play buttons, separate bottom Login/Profile button and themed board-pattern vector background. Navigation to existing match setup preserved.
- Match setup uses mint palette and updated emblem; rules, player mapping, shuffle selectors and launch logic preserved.
- Native Login screen uses a light-blue palette; same HTTPS auth and Android Keystore.
- Winner/Results uses gold celebration styling, visible upload control and the existing MatchUploader flow.
- Audio dialog and visible labels updated to Ludo Ho Jaye.
- Multiple CI builds passed through match setup. Run CI again after any new commit.

## Incomplete — NOT production ready
- Approved high-res glossy transparent PNG emblem has not been transferred into repository. Current native vector is a placeholder.
- Premium 3D 15x15 board and isolated white raised frame, proper four player-associated colored dice, glossy token sprites, humanoid and robot avatars, audio pack and final control icons are not yet wired.
- Profile, league card selection, fully modern settings and bottom-sheet pause remain in legacy form.
- Score approval auto-refresh from results still needs implementation.
- Manual Android device testing, Dynamic Home Shuffle regression testing and APK signoff remain outstanding.
- Never merge this draft PR until all above is complete and verified.
