package com.ludomaster.game;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClassicGameTest {
    @Test public void trackHas52UniqueSquares() {
        assertEquals(52,ClassicGame.TRACK.length);
        java.util.HashSet<String> cells=new java.util.HashSet<>();
        for(int[] pos:ClassicGame.TRACK) cells.add(pos[0]+","+pos[1]);
        assertEquals(52,cells.size());
    }
    @Test public void twoPlayerMatchHasOppositeColours() {
        ClassicGame game=new ClassicGame(2);
        assertTrue(game.active[0]);
        assertFalse(game.active[1]);
        assertTrue(game.active[2]);
        assertFalse(game.active[3]);
        assertEquals(-1,game.progress[0][0]);
        assertEquals(0,game.turn);
    }
    @Test public void firstStepsAtEachColorsStartSquare() {
        ClassicGame game=new ClassicGame(4);
        for(int color=0;color<4;color++) {
            game.progress[color][0]=0;
            assertArrayEquals(ClassicGame.TRACK[ClassicGame.START[color]],game.position(color,0));
        }
    }
    @Test public void finishingLanesAreReachableAndDistinct() {
        ClassicGame game=new ClassicGame(4);
        for(int color=0;color<4;color++) {
            for(int step=52;step<=57;step++) {
                game.progress[color][0]=step;
                assertArrayEquals(ClassicGame.LANES[color][step-52],game.position(color,0));
            }
        }
    }
    @Test public void normalMoveCannotExceedHome() {
        ClassicGame game=new ClassicGame(2);
        game.progress[0][0]=56;
        game.die=2;
        game.awaitingPawn=true;
        assertFalse(game.canMove(0));
        game.die=1;
        assertTrue(game.canMove(0));
    }
}
