package com.ludomaster.reference.shuffle;

import org.junit.Test;
import java.util.HashSet;
import static org.junit.Assert.*;

public class HomeShuffleEngineTest {
    @Test public void everyColourChangesEveryTime() {
        HomeShuffleEngine game = new HomeShuffleEngine(451);
        for (int n=0; n<1000; n++) {
            int[] before = game.assignment();
            game.requestShuffle();
            assertFalse(game.applyAtSafeBoundary(true));
            assertTrue(game.isPending());
            assertTrue(game.applyAtSafeBoundary(false));
            HashSet<Integer> slots = new HashSet<>();
            for (int i=0; i<4; i++) {
                assertNotEquals(before[i],game.homeSlot(i));
                slots.add(game.homeSlot(i));
            }
            assertEquals(4,slots.size());
        }
    }
    @Test public void startsEntrancesAndLanesAlwaysMoveTogether() {
        HomeShuffleEngine game = new HomeShuffleEngine(42);
        game.requestShuffle(); game.applyAtSafeBoundary(false);
        for (int colour=0; colour<4; colour++) {
            int slot=game.homeSlot(colour);
            assertEquals(HomeShuffleEngine.START[slot],game.startSquare(colour));
            assertEquals(HomeShuffleEngine.ENTRANCE[slot],game.entranceSquare(colour));
            assertEquals(HomeShuffleEngine.LANE_START[slot]+3,game.laneSquare(colour,3));
        }
        assertEquals(47,HomeShuffleEngine.forwardDistance(30,25));
        assertEquals(50,HomeShuffleEngine.ENTRANCE[0]);
        assertEquals(2,HomeShuffleEngine.forwardDistance(48,50));
        assertEquals(0,HomeShuffleEngine.forwardDistance(25,25));
    }
    @Test public void allowedIntervalsOnly() {
        for (int minutes:new int[]{1,2,3,5,10}) HomeShuffleEngine.validateMinutes(minutes);
        for (int minutes:new int[]{0,4,6,11}) {
            try {HomeShuffleEngine.validateMinutes(minutes);fail("accepted "+minutes);}
            catch(IllegalArgumentException expected) {}
        }
    }
}
