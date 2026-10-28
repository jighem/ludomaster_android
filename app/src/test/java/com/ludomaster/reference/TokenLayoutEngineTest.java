package com.ludomaster.reference;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenLayoutEngineTest {
    @Test public void boardTokenWidthTracksSquareAcrossScreenSizes() {
        float nominalWidth = 34f;
        float nominalHeight = 41f;
        for (float cell : new float[]{18f, 24f, 32f, 48f}) {
            float scale = TokenLayoutEngine.boardScale(cell, nominalWidth,nominalHeight);
            assertTrue(nominalWidth * scale <= cell * 0.88f + 0.01f);
            assertEquals(cell * 0.92f, nominalHeight * scale, 0.01f);
        }
    }

    @Test public void sharedSquareSizesUseRequestedRatios() {
        assertEquals(1.00f, TokenLayoutEngine.stackScale(1), 0.001f);
        assertEquals(0.74f, TokenLayoutEngine.stackScale(2), 0.001f);
        assertEquals(0.61f, TokenLayoutEngine.stackScale(3), 0.001f);
        assertEquals(0.51f, TokenLayoutEngine.stackScale(4), 0.001f);
    }

    @Test public void placementsStayCenteredAndRotateForVerticalTracks() {
        float cell = 30f;
        float sx = 0f;
        float sy = 0f;
        for (int i=0;i<4;i++) {
            TokenLayoutEngine.Offset horizontal = TokenLayoutEngine.offset(i,4,true,cell);
            TokenLayoutEngine.Offset vertical = TokenLayoutEngine.offset(i,4,false,cell);
            assertEquals(horizontal.x, vertical.y, 0.001f);
            assertEquals(horizontal.y, vertical.x, 0.001f);
            sx += horizontal.x;
            sy += horizontal.y;
        }
        assertEquals(0f, sx, 0.001f);
        assertEquals(0f, sy, 0.001f);
        assertTrue(Math.abs(TokenLayoutEngine.offset(0,2,true,cell).x) > 0f);
        assertTrue(Math.abs(TokenLayoutEngine.offset(0,2,false,cell).y) > 0f);
    }

    @Test public void playerCountsAndRepeatedShuffleRebasesKeepGroupsInsideCells() {
        for (int players : new int[]{2,3,4}) {
            for (int count=1;count<=players;count++) {
                float x=0f,y=0f;
                for(int index=0;index<count;index++) {
                    TokenLayoutEngine.Offset first=TokenLayoutEngine.offset(index,count,true,24f);
                    TokenLayoutEngine.Offset afterShuffle=TokenLayoutEngine.offset(index,count,true,24f);
                    assertEquals(first.x,afterShuffle.x,0.001f);
                    assertEquals(first.y,afterShuffle.y,0.001f);
                    x+=first.x;y+=first.y;
                    assertTrue(Math.abs(first.x)<=24f*0.22f+0.01f);
                    assertTrue(Math.abs(first.y)<=24f*0.22f+0.01f);
                }
                assertEquals(0f,x,0.001f);
                assertEquals(0f,y,0.001f);
            }
        }
    }
}
