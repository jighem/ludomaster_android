package com.ludomaster.reference;

import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;

/**
 * Ludo dice legality and turn-streak accounting. Plain rolls are uniform;
 * the weighted overload accepts only positive weights for valid outcomes.
 * No player-dependent weighting, hidden catch-up odds, artificial wins, or
 * device-specific outcome manipulation is built into this rule layer.
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
        return choose(permitted, null);
    }

    /** Weighted selection over permitted faces. Positive weights preserve every valid outcome. */
    public int roll(IntPredicate permitted, IntToDoubleFunction weightForFace) {
        if (weightForFace == null) throw new IllegalArgumentException("weightForFace");
        return choose(permitted, weightForFace);
    }

    private int choose(IntPredicate permitted, IntToDoubleFunction weightForFace) {
        if (permitted == null) throw new IllegalArgumentException("permitted");
        int max = consecutiveSixes >= 2 ? 5 : 6;
        int[] candidates = new int[max];
        double[] weights = new double[max];
        int count = 0;
        double totalWeight = 0d;
        for (int value=1;value<=max;value++) {
            if (!permitted.test(value)) continue;
            double weight = weightForFace == null ? 1d : weightForFace.applyAsDouble(value);
            if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0d)
                throw new IllegalArgumentException("Each permitted face needs a finite positive weight");
            candidates[count] = value;
            weights[count] = weight;
            totalWeight += weight;
            count++;
        }
        if (count==0) return 0;
        int result;
        if (count == 1) {
            result = candidates[0];
        } else if (weightForFace == null) {
            result = candidates[random.nextInt(count)];
        } else {
            double choice = random.nextDouble() * totalWeight;
            int selected = count - 1;
            for (int i=0;i<count;i++) {
                choice -= weights[i];
                if (choice < 0d) { selected = i; break; }
            }
            result = candidates[selected];
        }
        consecutiveSixes = result == 6 ? consecutiveSixes + 1 : 0;
        return result;
    }

    /** Called only when the actual turn changes to a different player. */
    public void startTurn() { consecutiveSixes = 0; }

    public int consecutiveSixes() { return consecutiveSixes; }
    public boolean nextRollExcludesSix() { return consecutiveSixes >= 2; }
}
