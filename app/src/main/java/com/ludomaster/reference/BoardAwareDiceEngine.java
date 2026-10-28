package com.ludomaster.reference;

/** Board-aware roll weighting, independent from board mutation and randomness. */
public final class BoardAwareDiceEngine {
    public enum Mode { FAIR_RANDOM, BALANCED_AGGRESSIVE }

    private final int aggressionPercent;

    public BoardAwareDiceEngine(int aggressionPercent) {
        this.aggressionPercent = Math.max(0, Math.min(100, aggressionPercent));
    }

    /** Tactical score is normalized to [0,1]; no face is ever assigned zero weight. */
    public double weight(Mode mode, double tacticalScore) {
        if (mode == null || mode == Mode.FAIR_RANDOM || aggressionPercent == 0) return 1d;
        double score = Math.max(0d, Math.min(1d, tacticalScore));
        return 1d + (aggressionPercent / 100d) * 2d * score;
    }

    /** Combine the current legal move's tactical opportunities into one bounded score. */
    public static double opportunityScore(boolean capture, boolean shieldedCapture,
                                          boolean escapeThreat, boolean safeDestination,
                                          boolean leaveHome, int laneProgress,
                                          boolean finishToken) {
        double score = 0d;
        if (capture) score += shieldedCapture ? 0.34d : 0.58d;
        if (escapeThreat) score += 0.20d;
        if (safeDestination) score += 0.20d;
        if (leaveHome) score += 0.12d;
        if (laneProgress > 0) score += 0.08d + Math.min(5, laneProgress) * 0.025d;
        if (finishToken) score += 0.35d;
        return Math.max(0d, Math.min(1d, score));
    }
}
