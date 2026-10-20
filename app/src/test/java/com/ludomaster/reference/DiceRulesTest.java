package com.ludomaster.reference;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class DiceRulesTest {
    private static final class MaxRollRandom extends Random {
        int lastBound;
        @Override public int nextInt(int bound) {
            lastBound = bound;
            return bound - 1;
        }
    }

    @Test public void thirdRollAfterTwoSixesIsLimitedToFive() {
        MaxRollRandom rng = new MaxRollRandom();
        DiceRules dice = new DiceRules(rng);
        assertEquals(6, dice.roll());
        assertEquals(6, dice.roll());
        assertTrue(dice.nextRollExcludesSix());
        assertEquals(5, dice.roll());
        assertEquals(5, rng.lastBound);
        assertEquals(0, dice.consecutiveSixes());
        assertEquals(6, dice.roll());
    }

    @Test public void newTurnResetsSixStreak() {
        DiceRules dice = new DiceRules(new MaxRollRandom());
        dice.roll(); dice.roll();
        dice.startTurn();
        assertEquals(0, dice.consecutiveSixes());
        assertEquals(6, dice.roll());
    }

    @Test public void allResultsStayWithinLegalDiceRange() {
        DiceRules dice = new DiceRules(new Random(71));
        for (int i = 0; i < 20000; i++) {
            if (i % 9 == 0) dice.startTurn();
            int face = dice.roll();
            assertTrue(face >= 1 && face <= 6);
        }
    }

    @Test public void regularRollSamplesAllSixFaces() {
        DiceRules dice = new DiceRules(new Random(19));
        boolean[] seen = new boolean[7];
        for (int i=0;i<2000;i++) {
            dice.startTurn();
            seen[dice.roll()]=true;
        }
        for(int value=1;value<=6;value++) assertTrue("Missing "+value,seen[value]);
    }
    @Test public void skipsForbiddenFacesWithoutBiasAmongRemaining() {
        DiceRules dice=new DiceRules(new MaxRollRandom());
        // Only 2 and 4 are legal; deterministic max-index selection chooses 4.
        assertEquals(4,dice.roll(face -> face==2 || face==4));
        assertEquals(4,dice.roll(face -> face==2 || face==4));
    }

    @Test public void fullyBlockedDieReturnsZeroAndDoesNotMutateStreak() {
        DiceRules dice=new DiceRules(new MaxRollRandom());
        dice.roll();dice.roll();
        assertEquals(0,dice.roll(face -> false));
        assertEquals(2,dice.consecutiveSixes());
        assertTrue(dice.nextRollExcludesSix());
    }

    @Test public void collisionFilteringNeverPermitsThirdSix() {
        DiceRules dice=new DiceRules(new MaxRollRandom());
        assertEquals(6,dice.roll(face -> true));
        assertEquals(6,dice.roll(face -> true));
        assertEquals(5,dice.roll(face -> face==5 || face==6));
    }

}
