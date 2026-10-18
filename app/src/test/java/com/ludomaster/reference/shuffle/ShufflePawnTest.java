package com.ludomaster.reference.shuffle;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShufflePawnTest {
    @Test public void remapPreservesTrackPositionAndRequiresForwardTravel() {
        ShufflePawn pawn=ShufflePawn.track(30,25);
        assertEquals(47,pawn.stepsToEntrance);
        assertEquals(30,pawn.remap(50).position);
        assertEquals(20,pawn.remap(50).stepsToEntrance);
    }
    @Test public void standingOnNewEntranceCannotRetroactivelyFinish() {
        ShufflePawn pawn=ShufflePawn.track(50,50);
        assertEquals(52,pawn.stepsToEntrance);
        assertEquals(51,pawn.advanceTrack(1).position);
    }
    @Test public void laneAndCompletedStatesStayUnchangedByShuffle() {
        ShufflePawn lane=ShufflePawn.lane(4);
        assertSame(lane,lane.remap(37));
        assertEquals(4,lane.position);
        ShufflePawn done=ShufflePawn.completed();
        assertSame(done,done.remap(11));
    }
}
