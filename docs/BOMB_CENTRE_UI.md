# Centre bomb control: device checks

Baseline: merged Android v0.4.2, including the foreground-audio fix. Test build: v0.4.3-bomb-ui.

## Behaviour
- Bomber Ludo only: the board centre shows a transparent unlit bomb icon and “Plant Bomb” before a roll.
- Tap to arm: matching lit-fuse artwork, gentle pulse, “Bomb Armed”. Tap again to cancel before rolling.
- Dice rolling and the central rolled number temporarily hide the control.
- When a human armed move still awaits a pawn selection, the lit indicator returns after the number clears. It is status-only; the choice is already locked.
- No eligible departure or no inventory: dimmed unavailable control. Bot turns cannot be toggled by human taps.
- The bottom planting button and text inventory strip are removed.
- Every pawn carrying its one bomb has a black shoulder dot with a thin white outline. It follows the pawn image through normal scaling, stacks, home, track, lane and completed states; it never intercepts taps.
- A successful plant removes only that pawn's dot immediately. Cancelled/failed planting does not spend inventory. Capture/re-entry does not restore spent inventory.
- Bomb planting, explosions, scoring, shields, automatic moves and the lifecycle-audio fix retain their existing rules.

## Validation
Android workflow must pass all tests and build the APK. BombControlStateTest verifies pre-roll toggling, unavailable states, number/rolling priority, locked armed restoration and variant/end-state exclusion. Existing BomberRulesTest verifies one bomb per pawn and non-refilling stock.

Physical-device checks in 2/3/4-player modes, small and large portrait screens:
1. Centre icon is centred, readable and easy to tap. Unlit and lit artwork are distinguishable without a white rectangle.
2. Arm/cancel before each roll (including six/capture extra rolls). Rolled number remains unobstructed.
3. With multiple legal armed pawns, wait until the number clears: armed status returns and cannot change the locked choice.
4. A forced automatic move still runs. Its bomb is planted once and only its dot disappears.
5. Check all four pawn colours and safe-square stacks. Dots move/resize with artwork and do not block selection.
6. Cancel arming, reject an ineligible move, capture an unused-bomb pawn, and return a spent-bomb pawn home: dots match real stock.
7. Classic/Home Shuffle have no bomb controls/dots. Hollow guided landing circles remain visible.
8. Background/exit during idle, armed, roll and result states: no continuing sound; pulse stops when window hides.

Keep this PR unmerged until the new APK appearance is accepted. No device-render pass is claimed by automated tests.
