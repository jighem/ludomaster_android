package com.ludomaster.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public class BombControlStateTest {
    private BombControlState state(boolean rolling,boolean number,boolean before,
            boolean armed,boolean eligible,boolean waiting) {
        return BombControlState.resolve(true,false,rolling,number,before,armed,eligible,waiting);
    }
    @Test public void beforeRollShowsUnlitHumanControl() {
        BombControlState result=state(false,false,true,false,true,false);
        assertEquals(BombControlState.IDLE,result);
        assertTrue(result.canToggle(false));assertFalse(result.canToggle(true));assertFalse(result.lit());
    }
    @Test public void armedCanBeCancelledBeforeRoll() {
        BombControlState result=state(false,false,true,true,true,false);
        assertEquals(BombControlState.ARMED,result);
        assertTrue(result.canToggle(false));assertTrue(result.lit());
    }
    @Test public void missingBombOrEligibleSquareDisablesPlanting() {
        BombControlState result=state(false,false,true,false,false,false);
        assertEquals(BombControlState.UNAVAILABLE,result);assertFalse(result.canToggle(false));
    }
    @Test public void rollAndRolledNumberTemporarilyOwnTheCentre() {
        assertEquals(BombControlState.HIDDEN,state(true,false,false,true,true,false));
        assertEquals(BombControlState.HIDDEN,state(false,true,false,true,true,true));
    }
    @Test public void armedIndicatorReturnsAfterNumberClearsButCannotToggle() {
        BombControlState result=state(false,false,false,true,true,true);
        assertEquals(BombControlState.ARMED_LOCKED,result);
        assertTrue(result.lit());assertFalse(result.canToggle(false));
    }
    @Test public void noUnarmedButtonWhileWaitingOrMoving() {
        assertEquals(BombControlState.HIDDEN,state(false,false,false,false,true,true));
        assertEquals(BombControlState.HIDDEN,state(false,false,false,true,true,false));
    }
    @Test public void otherVariantsAndEndedMatchesHaveNoControl() {
        assertEquals(BombControlState.HIDDEN,BombControlState.resolve(false,false,false,false,true,false,true,false));
        assertEquals(BombControlState.HIDDEN,BombControlState.resolve(true,true,false,false,true,true,true,false));
    }
}
