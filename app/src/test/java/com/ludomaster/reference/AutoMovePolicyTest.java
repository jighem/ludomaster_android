package com.ludomaster.reference;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class AutoMovePolicyTest {
    private AutoMovePolicy.Choice choice(int from,int to,boolean home,int bombs,String route) {
        return new AutoMovePolicy.Choice(from,to,home,bombs,route);
    }
    @Test public void oneLegalMoveRunsForEveryFace() {
        for(int face=1;face<=6;face++)assertEquals(0,AutoMovePolicy.automaticIndex(
            Collections.singletonList(choice(10,10+face,false,1,"track"))));
    }
    @Test public void interchangeableHomeExitsUseFirstPawn() {
        AutoMovePolicy.Choice home=choice(-1,0,true,1,"home");
        assertEquals(0,AutoMovePolicy.automaticIndex(Arrays.asList(home,home,home,home)));
    }
    @Test public void equivalentSafeStackUsesFirstPawn() {
        AutoMovePolicy.Choice stack=choice(8,12,false,-1,"same progress");
        assertEquals(0,AutoMovePolicy.automaticIndex(Arrays.asList(stack,stack)));
    }
    @Test public void homeExitVersusTrackMoveRequiresPlayerChoice() {
        assertEquals(-1,AutoMovePolicy.automaticIndex(Arrays.asList(
            choice(-1,0,true,1,"home"),choice(10,16,false,1,"track"))));
    }
    @Test public void differentBombStockIsMeaningfulEvenAtSameSquare() {
        assertEquals(-1,AutoMovePolicy.automaticIndex(Arrays.asList(
            choice(-1,0,true,0,"home"),choice(-1,0,true,1,"home"))));
    }
    @Test public void differentLaneProgressOrRoutesRequireChoice() {
        assertEquals(-1,AutoMovePolicy.automaticIndex(Arrays.asList(
            choice(8,12,false,1,"lap 1"),choice(8,12,false,1,"lap 2"))));
        assertEquals(-1,AutoMovePolicy.automaticIndex(Arrays.asList(
            choice(8,12,false,1,"track"),choice(21,25,false,1,"track"))));
    }
    @Test public void noLegalMoveDoesNotInventAMove() {
        assertEquals(-1,AutoMovePolicy.automaticIndex(Collections.emptyList()));
        assertEquals(-1,AutoMovePolicy.automaticIndex(null));
    }
}
