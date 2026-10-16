# Ludo Masters branding

- Product display name: **Ludo Masters**.
- Primary app emblem: provided glossy gold **LM shield** image.
- Splash and promotional artwork: provided **LUDO MASTERS** board-and-dice illustration.
- Included in this PR: `res/drawable/lm_shield.xml` is a temporary vector stand-in, not the supplied PNG.

## Import the user-supplied PNG files
The exact PNG images were delivered in conversation and are provided in a downloadable brand asset pack. Upload binary files to the repository with these exact paths:

1. `app/src/main/res/drawable-nodpi/lm_shield.png`
2. `app/src/main/res/drawable-nodpi/ludo_masters_splash.png`

Android resolves `R.drawable.lm_shield` from PNG and XML resource files with the same name only when there is no resource collision. **Delete** `app/src/main/res/drawable/lm_shield.xml` in the same PR as uploading the PNG. Do not leave both in the final build. Then add splash UI using `R.drawable.ludo_masters_splash` with aspect-ratio-preserving scaling.

For launcher icon, derive Android mipmap/adaptive icon layers from the shield, rather than using stretched full-splash artwork. Avoid using third-party trademarks in new screens.

## Product usage
Use the same LM shield as the app icon, game header and future settings/about screen. Use the full Ludo Masters illustrated logo on the splash/launch experience and promotional material. Do not obscure the live board with a large logo.
