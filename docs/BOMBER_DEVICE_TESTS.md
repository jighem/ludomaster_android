# Game variants and Bomber Ludo: device merge gates

Build: 0.4.1-bomber. PR #25 remains the gameplay baseline; current PR retains validated neon pawns and Guided Game.

## Approved rules

- Each pawn has one bomb for the entire match (four per player); death never refills them.
- Player arms before each roll, including extra rolls. Choice locks during rolling and movement.
- Selected pawn leaves a bomb on its departure square. Only unsafe main-track squares qualify; one bomb per square. Home yards, safe squares, finishing lanes and completed area cannot be mined.
- If no pawn can legally plant after the roll, cancel arming without spending a bomb and permit the normal move. Otherwise choose an eligible highlighted pawn.
- Only landing triggers a bomb. Any colour is vulnerable. Passing a bomb is harmless.
- Explosion consumes the bomb. Shield use is optional for humans; bots spend a shield automatically. A protected pawn may then resolve an ordinary capture on that square.
- An unprotected pawn returns home along the grid. Bomb owner receives one kill and victim one death; own-colour explosion records death only. Common scoring option applies +5/-5; a credited kill earns a shield when shields are enabled.
- Sixes retain the existing extra-roll rule even after a bomb death. Bomb kills do not give the absent bomb owner an out-of-turn roll.
- Bomb inventory and positions are match-scoped. Classic and Home Shuffle have no bombs. Home Shuffle and Bomber are separate variants.

## Automated coverage

BomberRulesTest covers all 76 board indexes plus home, all safe squares, one bomb for each of four independent pawns, no replenishment, duplicate placement, no spend on rejection, landing-only single consumption, own/enemy credit, shield consumption and match isolation. MatchVariantTest covers explicit mode exclusivity and old shuffle intents. Existing dice, bot, token and shuffle suites remain required.

## Roll and guide usability

- Button reads “Plant Bomb with this Roll”, with a bomb icon. Armed state offers cancellation before rolling.
- Arming displays “BOMB ARMED / Plants with this roll” persistently in the board centre. Cancelling clears it; rolling replaces it with the number.
- Auto-run chooses a sole legal move or the first of interchangeable pawns, including home exits and equivalent safe stacks. Meaningful source/destination, route or bomb-stock differences still require a choice.
- Auto-run allows a short preview, works with Guided Game on/off and validates the roll/turn again before executing. Manual input invalidates the queued move.
- Landing previews are hollow circles with a contrasting rim and the existing red/green/pawn-colour semantics; step numbers remain visible.

## Physical-device matrix

Run in Computer and Pass & Play, with 2/3/4 players. Repeat on small and large portrait Android screens.

| Check | Pass criterion | Main risk |
| --- | --- | --- |
| Setup navigation | Player count → game type → player setup; back retains selection; only scoring/shields/guide are optional | Wrong variant or dropped league/player choice |
| Classic | No bomb controls or shuffle countdown; normal move/capture/finish works | Common-path regression |
| Home Shuffle | Interval shown only here; repeat swaps retain tokens, lanes, dice ownership and guide | Old shuffle flag |
| Bomber arming | Arm/cancel before roll; cannot change after roll; correct moving pawn spends exactly one | Late planting or repeated spend |
| Restricted squares | Safe/home/lane/completed/existing-bomb source cannot plant; no stock spent | Illegal mines |
| Stock | One independently per pawn; remains spent after capture and re-entry; new match resets | Refill or cross-pawn stock |
| Visibility/taps | Owner-colour bomb rim visible, underlying pawns selectable, hold pawn shows ID/stock | Overlay intercepts input or small-screen crowding |
| Detonation | Passing harmless; own/enemy landing explodes once; only landing pawn dies | Repeated trigger or wrong victim |
| Shields | Spend blocks death and consumes bomb; decline causes death; bot resolves without human prompt | Stuck turn/dialog or double shield spend |
| Scoring | Enemy +1 K/+5, victim +1 D/−5; own explosion only death; points zero with scoring off | Wrong owner credit |
| Turn settlement | Wait for explosion and return before continuing; six extra roll respected; no bomb-owner extra turn | Animation/turn race |
| Guides/bots | Hollow landing circles visible in all colours; red bomb danger, amber when shield available; numbered destinations accurate | Misleading preview or bot stall |
| Auto-run | One track pawn on a non-six moves automatically; equivalent home/stack pawns use first; distinct choices wait; no stale/double movement after manual input | Lost strategic choice or queued move race |
| Armed feedback | Centre message appears/cancels before roll and becomes the rolled number; planting button is clear | Stale overlay or unnoticed arming |
| End/results | Finish game, inspect K/D/shields/points and saved variant metadata; retry upload still works | Scoring/save regression |

Automated checks do not substitute for these device checks. Keep the PR unmerged until accepted.
