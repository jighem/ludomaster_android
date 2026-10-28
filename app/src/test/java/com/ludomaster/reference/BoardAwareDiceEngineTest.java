package com.ludomaster.reference;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class BoardAwareDiceEngineTest {
    @Test public void fairModeAndNeutralBoardKeepUniformWeights() {
        BoardAwareDiceEngine engine=new BoardAwareDiceEngine(60);
        for(int face=1;face<=6;face++) {
            assertEquals(1d,engine.weight(BoardAwareDiceEngine.Mode.FAIR_RANDOM,1d),0.0001d);
            assertEquals(1d,engine.weight(BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE,0d),0.0001d);
        }
    }

    @Test public void aggressiveWeightIsBoundedAndNeverRemovesAnOutcome() {
        BoardAwareDiceEngine engine=new BoardAwareDiceEngine(60);
        assertEquals(1d,engine.weight(BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE,-1d),0.0001d);
        assertEquals(2.2d,engine.weight(BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE,1d),0.0001d);
        assertTrue(engine.weight(BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE,0.5d)>0d);
    }

    @Test public void runOneHundredThousandRollsPerModeAndRecordDistribution() {
        final int rolls=100_000;
        BoardAwareDiceEngine engine=new BoardAwareDiceEngine(60);
        for(BoardAwareDiceEngine.Mode mode:BoardAwareDiceEngine.Mode.values()) {
            DiceRules rules=new DiceRules(new Random(mode.ordinal()+98));
            int[] counts=new int[7];
            for(int i=0;i<rolls;i++) {
                rules.startTurn();
                int result=rules.roll(face->true,face->
                    engine.weight(mode,face==1?1d:0d));
                counts[result]++;
            }
            int total=0;
            for(int face=1;face<=6;face++) { assertTrue(counts[face]>0);total+=counts[face]; }
            assertEquals(rolls,total);
            double firstFaceRate=counts[1]/(double)rolls;
            if(mode==BoardAwareDiceEngine.Mode.FAIR_RANDOM)
                assertEquals(1d/6d,firstFaceRate,0.008d);
            else
                assertTrue("Tactical weighting should move probability without guaranteeing it",
                    firstFaceRate>1d/6d && firstFaceRate<0.35d);
        }
    }
}
