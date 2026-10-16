package com.ludomaster.game;

/** Immutable optional house-rule settings for one offline match. */
public final class MatchOptions {
    public final boolean combatScoring;
    public final boolean immunity;
    public final boolean dynamicHomeShuffle;
    public final int shuffleIntervalSeconds;
    public MatchOptions(boolean combatScoring, boolean immunity, boolean dynamicHomeShuffle) {
        this.combatScoring = combatScoring;
        this.immunity = immunity;
        this.dynamicHomeShuffle = dynamicHomeShuffle;
        this.shuffleIntervalSeconds = 300;
    }
}
