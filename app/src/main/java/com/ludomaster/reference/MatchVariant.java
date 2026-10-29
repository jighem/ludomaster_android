package com.ludomaster.reference;

/** Match variant is independent of player count and computer/pass-and-play mode. */
public enum MatchVariant {
    CLASSIC("classic", "Classic Ludo"),
    HOME_SHUFFLE("home_shuffle", "Home Shuffle Ludo"),
    BOMBER("bomber", "Bomber Ludo");

    public final String id;
    public final String title;
    MatchVariant(String id,String title) { this.id=id;this.title=title; }
    public static MatchVariant from(String id,boolean legacyShuffle) {
        if(id==null) return legacyShuffle ? HOME_SHUFFLE : CLASSIC;
        for(MatchVariant variant:values()) if(variant.id.equals(id)) return variant;
        return CLASSIC;
    }
}
