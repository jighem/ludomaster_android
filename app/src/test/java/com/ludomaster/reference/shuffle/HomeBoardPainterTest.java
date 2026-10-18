package com.ludomaster.reference.shuffle;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeBoardPainterTest {
    @Test public void sourceSlotsFollowClockwiseBoardRotation() {
        for (int turns=0;turns<4;turns++) {
            boolean[] seen=new boolean[4];
            for(int source=0;source<4;source++) {
                int screen=HomeBoardPainter.rotatedPhysicalSlot(source,turns);
                assertEquals((source+turns)%4,screen);
                assertFalse(seen[screen]);
                seen[screen]=true;
            }
        }
    }

    @Test public void homeMappingMatchesOriginalImageAtZeroRotation() {
        assertEquals(0,HomeBoardPainter.rotatedPhysicalSlot(0,0)); // yellow upper right
        assertEquals(1,HomeBoardPainter.rotatedPhysicalSlot(1,0)); // blue lower right
        assertEquals(2,HomeBoardPainter.rotatedPhysicalSlot(2,0)); // red lower left
        assertEquals(3,HomeBoardPainter.rotatedPhysicalSlot(3,0)); // green upper left
    }

    @Test public void clockwiseQuarterTurnMovesUpperRightToLowerRight() {
        assertEquals(1,HomeBoardPainter.rotatedPhysicalSlot(0,1));
        assertEquals(2,HomeBoardPainter.rotatedPhysicalSlot(1,1));
        assertEquals(3,HomeBoardPainter.rotatedPhysicalSlot(2,1));
        assertEquals(0,HomeBoardPainter.rotatedPhysicalSlot(3,1));
    }
}
