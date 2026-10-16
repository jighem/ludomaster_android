package com.ludomaster.game;

import org.junit.Test;
import static org.junit.Assert.*;

public class DynamicHomeShuffleTest {
    @Test public void trackPawnsKeepExactPhysicalPosition() {
        ClassicGame game = new ClassicGame(4,new MatchOptions(false,false,true));
        game.progress[0][0]=30; game.progress[1][0]=16;
        game.progress[2][0]=51; game.progress[3][0]=3;
        int[][] before=new int[4][];
        for(int c=0;c<4;c++) before[c]=game.position(c,0);
        game.requestHomeShuffle();
        assertEquals(1,game.shuffleCount);
        for(int c=0;c<4;c++) assertArrayEquals(before[c],game.position(c,0));
        assertFalse(java.util.Arrays.equals(new int[]{0,1,2,3},game.homeQuadrant));
    }
    @Test public void shuffleChangesYardCoordinatesButNotPawnOwner() {
        ClassicGame game=new ClassicGame(4,new MatchOptions(false,false,true));
        int[][] prior=new int[4][];
        for(int c=0;c<4;c++) prior[c]=game.position(c,0);
        game.requestHomeShuffle();
        int changed=0;
        for(int c=0;c<4;c++) if(!java.util.Arrays.equals(prior[c],game.position(c,0)))changed++;
        assertTrue(changed>=2);
        for(int c=0;c<4;c++) assertEquals(-1,game.progress[c][0]);
    }
    @Test public void finishingLanePawnMovesWithItsColourWithoutLosingProgress() {
        ClassicGame game=new ClassicGame(4,new MatchOptions(false,false,true));
        // Select any colour whose home changes on the next shuffle.
        int[][] before = new int[4][];
        for(int color=0;color<4;color++) {
            game.progress[color][0]=54;
            before[color]=game.position(color,0);
        }
        game.requestHomeShuffle();
        int changed=0;
        for(int color=0;color<4;color++) {
            assertEquals(54,game.progress[color][0]);
            assertArrayEquals(ClassicGame.LANES[game.homeQuadrant[color]][2],game.position(color,0));
            if(!java.util.Arrays.equals(before[color],game.position(color,0)))changed++;
        }
        assertTrue(changed>=2);
    }
    @Test public void completedTokenRemainsCompletedAfterHomeSwap() {
        ClassicGame game=new ClassicGame(4,new MatchOptions(false,false,true));
        game.progress[0][0]=57;
        game.requestHomeShuffle();
        assertEquals(57,game.progress[0][0]);
        assertArrayEquals(ClassicGame.LANES[game.homeQuadrant[0]][5],game.position(0,0));
    }
    @Test public void waitingMoveDefersShuffleUntilMoveResolves() {
        ClassicGame game=new ClassicGame(2,new MatchOptions(false,false,true));
        game.turn=0;game.progress[0][0]=3;game.die=2;game.awaitingPawn=true;
        game.requestHomeShuffle();
        assertEquals(0,game.shuffleCount);
        assertTrue(game.move(0));
        assertEquals(1,game.shuffleCount);
    }
    @Test public void disabledModeNeverShuffles() {
        ClassicGame game=new ClassicGame(2,new MatchOptions(false,false,false));
        game.requestHomeShuffle();
        assertEquals(0,game.shuffleCount);
    }
}
