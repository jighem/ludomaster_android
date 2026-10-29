package com.ludomaster.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchVariantTest {
    @Test public void newSelectionOverridesOldShuffleFlag() {
        assertEquals(MatchVariant.CLASSIC,MatchVariant.from("classic",true));
        assertEquals(MatchVariant.BOMBER,MatchVariant.from("bomber",true));
        assertEquals(MatchVariant.HOME_SHUFFLE,MatchVariant.from("home_shuffle",false));
    }
    @Test public void oldLaunchesKeepTheirValidatedVariant() {
        assertEquals(MatchVariant.CLASSIC,MatchVariant.from(null,false));
        assertEquals(MatchVariant.HOME_SHUFFLE,MatchVariant.from(null,true));
        assertEquals(MatchVariant.CLASSIC,MatchVariant.from("unknown",true));
    }
}
