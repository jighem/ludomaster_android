package com.ludomaster.reference;

import java.util.Random;
import java.util.function.IntPredicate;

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
        return roll(face -> true);
    }

    /**
     * Uniformly choose from the currently permitted dice faces.
     * The caller may prohibit a face only when its outcome is unusable under
     * the current game rules, never based on player identity or score.
     * Returns 0 if no face is permitted; caller should safely pass the turn.
     */
    public int roll(IntPredicate permitted) {
        if (permitted == null) throw new IllegalArgumentException("permitted");
        int max = consecutiveSixes >= 2 ? 5 : 6;
        int[] candidates = new int[max];
        int count = 0;
        for (int value=1;value<=max;value++)
            if (permitted.test(value)) candidates[count++] = value;
        if (count==0) return 0;
        int result=candidates[random.nextInt(count)];
        consecutiveSixes = result == 6 ? consecutiveSixes + 1 : 0;
        return result;
    }

    /** Called only when the actual turn changes to a different player. */
    public void startTurn() { consecutiveSixes = 0; }

    public int consecutiveSixes() { return consecutiveSixes; }
    public boolean nextRollExcludesSix() { return consecutiveSixes >= 2; }
}
