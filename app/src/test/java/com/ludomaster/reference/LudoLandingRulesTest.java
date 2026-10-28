package com.ludomaster.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public class LudoLandingRulesTest {
    private final int[] lane={52,53,54,55,56,57};

    @Test public void trackWrapsFrom51ToZero() {
        assertEquals(1,LudoLandingRules.destination(50,39,true,false,0,10,37,true,3,lane));
    }

    @Test public void entersLaneAfterPhysicalEntranceInShuffle() {
        assertEquals(52,LudoLandingRules.destination(49,39,true,false,0,2,50,true,3,lane));
    }

    @Test public void legacyFinishingLaneIsPreserved() {
        assertEquals(54,LudoLandingRules.destination(49,39,true,false,0,50,50,false,4,lane));
    }

    @Test public void pawnsCanStackOnlyOnSafeSquares() {
        assertTrue(LudoLandingRules.occupiedByFriend(11,false,new int[]{4,11,15}));
        assertFalse(LudoLandingRules.occupiedByFriend(11,true,new int[]{11}));
        assertFalse(LudoLandingRules.occupiedByFriend(12,false,new int[]{11}));
        assertFalse(LudoLandingRules.occupiedByFriend(12,false,new int[]{}));
    }

    @Test public void finishedLaneBlocksUnsafeFriendlyLanding() {
        assertTrue(LudoLandingRules.occupiedByFriend(56,false,new int[]{56}));
        assertFalse(LudoLandingRules.occupiedByFriend(56,true,new int[]{56}));
    }
    @Test public void guidedPrefixesCountAcrossEntranceAndLane() {
        int[] expected={50,51,52,53,54,55};
        for(int steps=1;steps<=6;steps++)
            assertEquals(expected[steps-1],LudoLandingRules.destination(
                49,0,true,false,0,2,51,true,steps,lane));
    }

    @Test public void homeExitPreviewUsesCurrentShuffledStart() {
        for(int start:new int[]{0,13,26,39})
            assertEquals(start,LudoLandingRules.destination(
                -1,start,false,false,0,50,50,true,6,lane));
    }

    @Test public void guidedExactFinishAndOvershoot() {
        assertEquals(57,LudoLandingRules.destination(55,0,true,true,4,0,50,true,2,lane));
        assertEquals(-1,LudoLandingRules.destination(55,0,true,true,4,0,50,true,3,lane));
    }
}
