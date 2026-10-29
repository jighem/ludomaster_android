package com.ludomaster.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public class BomberRulesTest {
    @Test public void eachPawnHasOneNonRefillingBomb() {
        BomberRules game=new BomberRules();
        int placed=0;
        for(int pawn=0;pawn<4;pawn++) {
            String id="red:"+pawn;
            assertEquals(1,game.remaining(id));
            assertTrue(game.plant(id,"red",1,true,false));
            placed++;
            assertEquals(0,game.remaining(id));
            assertNotNull(game.detonate(1));
            assertFalse(game.plant(id,"red",2,true,false));
            // Capture/home/re-entry do not introduce an inventory reset.
            assertFalse(game.canPlant(id,-1,false,false));
            assertFalse(game.canPlant(id,2,true,false));
            assertEquals(0,game.remaining(id));
        }
        assertEquals(4,placed);
        assertEquals(1,game.remaining("green:0"));
    }
    @Test public void onlyUnsafeMainTrackAcceptsBombs() {
        BomberRules game=new BomberRules();
        for(int square=-1;square<76;square++) {
            boolean expected=square>=0 && square<52 &&
                square!=0 && square!=8 && square!=13 && square!=21 &&
                square!=26 && square!=34 && square!=39 && square!=47;
            assertEquals("square "+square,expected,game.canPlant("pawn",square,true,false));
        }
        assertFalse(game.canPlant("pawn",1,false,false));
        assertFalse(game.canPlant("pawn",1,true,true));
        assertEquals(1,game.remaining("pawn"));
    }
    @Test public void noDuplicateAndFailedPlantDoesNotSpend() {
        BomberRules game=new BomberRules();
        assertTrue(game.plant("red:0","red",1,true,false));
        assertFalse(game.plant("blue:0","blue",1,true,false));
        assertEquals(1,game.remaining("blue:0"));
        assertEquals("red",game.bombs().get(1).ownerColor);
        assertNotNull(game.detonate(1));
        assertTrue(game.plant("blue:0","blue",1,true,false));
    }
    @Test public void passingSquaresDoNotTriggerAndLandingConsumesOnce() {
        BomberRules game=new BomberRules();
        game.plant("red:0","red",2,true,false);
        // A move passes 2 but lands on 4: only its destination is resolved.
        assertNull(game.detonate(4));
        assertTrue(game.hasBomb(2));
        assertNotNull(game.detonate(2));
        assertNull(game.detonate(2));
        assertFalse(game.hasBomb(2));
    }
    @Test public void enemyGetsKillButOwnColourHasOnlyDeathPenalty() {
        BomberRules game=new BomberRules();
        game.plant("red:0","red",3,true,false);
        BomberRules.Bomb bomb=game.detonate(3);
        assertEquals("red",BomberRules.creditedKiller(bomb,"blue"));
        assertNull(BomberRules.creditedKiller(bomb,"red"));
    }
    @Test public void shieldedLandingStillConsumesBombWithoutRefund() {
        BomberRules game=new BomberRules();
        game.plant("red:0","red",3,true,false);
        assertNotNull(game.detonate(3)); // Shield decision happens after consumption.
        assertFalse(game.hasBomb(3));
        assertEquals(0,game.remaining("red:0"));
    }
    @Test public void bombSnapshotIsImmutableAndMatchesAreIsolated() {
        BomberRules first=new BomberRules(),second=new BomberRules();
        first.plant("pawn","red",1,true,false);
        assertFalse(second.hasBomb(1));
        assertEquals(1,second.remaining("pawn"));
        try { first.bombs().clear();fail("immutable"); } catch(UnsupportedOperationException expected) { }
        assertTrue(first.hasBomb(1));
    }
}
