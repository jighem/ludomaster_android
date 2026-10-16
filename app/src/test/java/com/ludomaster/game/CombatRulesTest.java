package com.ludomaster.game;

import org.junit.Test;
import static org.junit.Assert.*;

public class CombatRulesTest {
    @Test public void captureAwardsPointsAndShield() {
        ClassicGame g = new ClassicGame(2, new MatchOptions(true,true,false));
        // Red at board cell 5 (progress 4), Yellow at same destination after Red moves 1.
        g.progress[0][0]=4;
        g.progress[2][0]=31; // Yellow start 26 + 31 = cell 5.
        g.turn=0; g.die=1; g.awaitingPawn=true;
        assertTrue(g.move(0));
        assertEquals(-1,g.progress[2][0]);
        assertEquals(1,g.captures[0]);
        assertEquals(1,g.deaths[2]);
        assertEquals(5,g.combatPoints(0));
        assertEquals(-5,g.combatPoints(2));
        assertEquals(1,g.shields(0));
    }
    @Test public void shieldMayBlockCaptureAndPreserveToken() {
        ClassicGame g = new ClassicGame(2, new MatchOptions(true,true,false));
        g.ledgers[2].recordCapture(); // Defender already earned a shield.
        g.progress[0][0]=4; g.progress[2][0]=31;
        g.turn=0;g.die=1;g.awaitingPawn=true;
        assertTrue(g.move(0));
        assertEquals(2,g.pendingDefender);
        assertEquals(31,g.progress[2][0]);
        g.decideShield(true);
        assertEquals(-1,g.pendingDefender);
        assertEquals(31,g.progress[2][0]);
        assertEquals(0,g.captures[0]);
        assertEquals(0,g.deaths[2]);
        assertEquals(0,g.shields(2));
    }
    @Test public void defenderMayDeclineImmunity() {
        ClassicGame g = new ClassicGame(2, new MatchOptions(true,true,false));
        g.ledgers[2].recordCapture();
        g.progress[0][0]=4;g.progress[2][0]=31;
        g.turn=0;g.die=1;g.awaitingPawn=true;g.move(0);
        g.decideShield(false);
        assertEquals(-1,g.progress[2][0]);
        assertEquals(1,g.captures[0]);
        assertEquals(1,g.shields(0));
        assertEquals(1,g.shields(2));
    }
    @Test public void disabledCombatStillCountsCaptures() {
        ClassicGame g = new ClassicGame(2,new MatchOptions(false,false,false));
        g.progress[0][0]=4;g.progress[2][0]=31;
        g.turn=0;g.die=1;g.awaitingPawn=true;g.move(0);
        assertEquals(1,g.captures[0]);
        assertEquals(0,g.combatPoints(0));
        assertEquals(0,g.shields(0));
    }
}
