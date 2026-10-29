package com.ludomaster.reference;

import java.util.List;
import java.util.Random;

/** Ranks already-legal bot moves. It does not execute or validate gameplay moves. */
public final class StrategicBot {
    private StrategicBot() {}

    public static final class Move {
        public final boolean legal;
        public final boolean capture;
        public final boolean shieldedCapture;
        public final boolean finish;
        public final boolean escapeThreat;
        public final boolean safeDestination;
        public final boolean leaveHome;
        public final boolean threatenedDestination;
        public final int progress;

        public Move(boolean legal, boolean capture, boolean shieldedCapture, boolean finish,
                    boolean escapeThreat, boolean safeDestination, boolean leaveHome,
                    boolean threatenedDestination, int progress) {
            this.legal=legal;
            this.capture=capture;
            this.shieldedCapture=shieldedCapture;
            this.finish=finish;
            this.escapeThreat=escapeThreat;
            this.safeDestination=safeDestination;
            this.leaveHome=leaveHome;
            this.threatenedDestination=threatenedDestination;
            this.progress=progress;
        }
    }

    public static int score(Move move) {
        if (move == null || !move.legal) return Integer.MIN_VALUE;
        int score=0;
        if (move.capture) score += move.shieldedCapture ? 65 : 100;
        if (move.finish) score += 125;
        if (move.escapeThreat) score += 80;
        if (move.safeDestination) score += 52;
        if (move.leaveHome) score += 30;
        score += Math.max(0, Math.min(20, move.progress));
        if (move.threatenedDestination && !move.safeDestination) score -= 32;
        return score;
    }

    /** Scores become positive weights so strong moves are favored without being forced. */
    public static double selectionWeight(Move move) {
        int score=score(move);
        return score==Integer.MIN_VALUE ? 0d : 1d + Math.max(0,score);
    }

    /** Weighted sampling returns -1 when there is no legal candidate. */
    public static int selectWeightedIndex(List<Move> moves, Random random) {
        if (moves == null || random == null) throw new IllegalArgumentException();
        double total=0d;
        for (int i=0;i<moves.size();i++) {
            total+=selectionWeight(moves.get(i));
        }
        if(total<=0d) return -1;
        double pick=random.nextDouble()*total;
        for(int i=0;i<moves.size();i++) {
            pick-=selectionWeight(moves.get(i));
            if(pick<0d) return i;
        }
        return -1;
    }
}
