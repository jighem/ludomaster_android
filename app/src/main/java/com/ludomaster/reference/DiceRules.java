package com.ludomaster.reference;

import java.util.Random;

/**
 * Fair offline Ludo dice rules. No player-dependent weighting, hidden catch-up
 * odds, artificial wins, or device-specific outcome manipulation.
 *
 * House rule: after two consecutive sixes within one turn, the next result
 * is uniformly sampled from 1..5. A non-six ends the streak. Extra turns due
 * to capture/finish do not reset this streak until the player's turn ends.
 */
public final class DiceRules {
    private final Random random;
    private int consecutiveSixes;

    public DiceRules(Random random) {
        if (random == null) throw new IllegalArgumentException("random");
        this.random = random;
    }

    public int roll() {
        int max = consecutiveSixes >= 2 ? 5 : 6;
        int value = random.nextInt(max) + 1;
        consecutiveSixes = value == 6 ? consecutiveSixes + 1 : 0;
        return value;
    }

    /** Called only when the actual turn changes to a different player. */
    public void startTurn() { consecutiveSixes = 0; }

    public int consecutiveSixes() { return consecutiveSixes; }
    public boolean nextRollExcludesSix() { return consecutiveSixes >= 2; }
}
