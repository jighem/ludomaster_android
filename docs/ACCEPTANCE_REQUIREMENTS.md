# Ludo Masters Android — acceptance requirements (approved by project owner)

This is the governing specification for the Android app. Do not replace the approved reference presentation with a simplified board. All work in this Android repository is independent from the web repository `jighem/Ludomasters`.

## Source, appearance and branding
- Base the native app on the author-approved Android source, retaining its board, token visuals, dice, animations, screens and local play mechanics where compatible with the feature requirements.
- Product name everywhere: **Ludo Masters** (application ID may remain `com.ludomaster.android`).
- Replace all original game-name text in source, resources, metadata and filenames. Replace all crown graphics and crown-winning indicators with the supplied gold **LM shield**; use the supplied illustrated **Ludo Masters** logo for splash/marketing. **Do not assume string replacements remove text baked into PNG, JPEG, GIF, video or audio. Audit and replace those assets individually.**
- Do not ship an APK until the actual screenshot matches the agreed reference presentation.

## Offline and networking
- V1 game itself is **fully offline pass-and-play** for 2, 3 or 4 players on one device.
- No original game's production server, third-party multiplayer, remote dice control or external-account dependencies.
- No hidden network calls, embedded secrets, arbitrary dice control or data transmission while offline.
- Optional network access **only to our own** `https://ludohojaye.com` HTTPS result-sync API after the match, with a visible consent/sync action. Offline play must never require login or internet.
- Result queue must persist locally across app restarts; idempotent UUID per match and secure API authentication once that feature is built.

## Post-match reconciliation
- Record game mode, player slot 1..4, colour, player-entered offline name, finish rank, kills, deaths, shields consumed, points, duration, shuffle history and a unique match ID.
- Before upload, let the user choose which existing registered league member corresponds to **each** offline player, because offline names may differ.
- Do not auto-associate accounts based on similar names. Validate no two local players map to the same league account; provide an unmapped/pending option.
- Differentiate self-reported offline results from server-verified live match results. Recommend league-admin approval before counting towards official tournaments or standings.

## Independently switchable optional V1 rules
1. **Combat scoring:** +5 points for each successful enemy pawn capture, -5 points for each own pawn captured. Track separately from finishing-rank score. Turning it off must not disable captures.
2. **Earned immunity:** each successful capture gives the capturing player **one shield**. When a player's pawn is threatened with capture and a shield is available, ask the defending player to spend a shield to block capture or accept capture; if blocked, pawn remains, no kill/death points or extra turn from that blocked capture. Save total earned/used shields. Shield cap not specified: do not silently invent one.
3. **Dynamic Home Shuffle:** independently optional, timer = **five minutes**, first shuffle five minutes after match starts, then every five minutes while active. Randomly reassign colour homes, starting squares and finish lanes. Track pawns retain exact physical square. Pawns still in their yard move with colour. Pawns *already in finish lane* move **with their colour's newly assigned finishing lane**, retaining lane depth. Completed pawns remain completed. Starting squares and finishing entrances follow their colour. All uncompleted main-track pawns always move **forward-only**, and must complete another forward lap if already past new finish entrance. A pending shuffle waits until current move and immunity/capture resolution complete. Preserve whose turn it is. Pause/resume/clock handling should be specified and tested before release.
- Backwards movement is **future-only**, not V1.

## Test gates for a release APK
- Original source imported and compiled cleanly; no legacy strings/assets appear in user-visible gameplay.
- Match starts, rolls, pawns move/capture/finish correctly using reference visuals.
- Combat scoring, shield decision, and shuffle on/off all have passing unit tests and on-device manual tests.
- Random swap preserves every track pawn's absolute cell; finishing-lane pawns move with their colour, while completed tokens retain completion.
- Offline airplane-mode match completion works end-to-end.
- Result mapping and HTTPS sync verified with backend (later milestone).
