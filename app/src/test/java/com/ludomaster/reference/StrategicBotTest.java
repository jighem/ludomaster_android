package com.ludomaster.reference;

import org.junit.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.*;

public class StrategicBotTest {
    @Test public void ranksFinishCaptureAndEscapeAboveOrdinaryAdvance() {
        StrategicBot.Move ordinary=new StrategicBot.Move(true,false,false,false,false,false,false,false,12);
        StrategicBot.Move capture=new StrategicBot.Move(true,true,false,false,false,false,false,false,2);
        StrategicBot.Move finish=new StrategicBot.Move(true,false,false,true,false,false,false,false,0);
        StrategicBot.Move escape=new StrategicBot.Move(true,false,false,false,true,false,false,false,1);
        assertTrue(StrategicBot.score(capture)>StrategicBot.score(ordinary));
        assertTrue(StrategicBot.score(finish)>StrategicBot.score(capture));
        assertTrue(StrategicBot.score(escape)>StrategicBot.score(ordinary));
    }

    @Test public void rejectsIllegalCandidatesAndPenalizesThreatenedUnsafeLanding() {
        StrategicBot.Move illegal=new StrategicBot.Move(false,true,false,true,false,true,false,false,20);
        StrategicBot.Move unsafe=new StrategicBot.Move(true,false,false,false,false,false,false,true,2);
        StrategicBot.Move safe=new StrategicBot.Move(true,false,false,false,false,true,false,false,2);
        assertEquals(Integer.MIN_VALUE,StrategicBot.score(illegal));
        assertTrue(StrategicBot.score(safe)>StrategicBot.score(unsafe));
        assertEquals(-1,StrategicBot.selectWeightedIndex(Arrays.asList(illegal),new Random(1)));
    }

    @Test public void weightedChoiceFavorsButDoesNotGuaranteeTheTacticalMove() {
        StrategicBot.Move ordinary=new StrategicBot.Move(true,false,false,false,false,false,false,false,2);
        StrategicBot.Move finish=new StrategicBot.Move(true,false,false,true,false,false,false,false,0);
        Random random=new Random(19);
        int finishes=0;
        for(int i=0;i<10_000;i++)
            if(StrategicBot.selectWeightedIndex(Arrays.asList(ordinary,finish),random)==1) finishes++;
        assertTrue(finishes>7_000);
        assertTrue(finishes<10_000);
    }
}
