# Native Android source migration

The user reports the upstream author expressly permitted modification and reuse.
This migration copies the reference app's native Java activities, layouts and game assets rather than using the previous simplified board.

## Important next steps
- Audit and remove online/remote-control entry points, tracking, and any unwanted endpoints.
- Replace all embedded raster-image text and original branding with the user's Ludo Masters artwork and LM shield.
- Confirm all prior optional rules (combat, immunity, dynamic home shuffle) are re-integrated into the imported game engine; they are **not** currently wired into the imported source.
- Verify 2–4 player offline pass-and-play with emulator and real devices.
- Keep repository private until rights and redistribution scope are documented.

Do not distribute APK until these issues are complete.
