package com.ludomaster.reference;

/** Pure destination checks matching the existing movement loop. */
public final class LudoLandingRules {
    private LudoLandingRules() {}

    public static int destination(int currentSquare, int startSquare, boolean alive,
                                  boolean alreadyInLane, int laneIndex,
                                  int remainingToEntrance, int endSquare,
                                  boolean shuffle, int steps, int[] lane) {
        if (steps < 1 || steps > 6) throw new IllegalArgumentException("dice");
        if (!alive) return startSquare;
        int square=currentSquare;
        boolean inLane=alreadyInLane;
        for(int i=0;i<steps;i++) {
            if (inLane) {
                if (lane==null || laneIndex<0 || laneIndex>=lane.length) return -1;
                square=lane[laneIndex++];
            } else {
                square=(square+1)%52;
                if (shuffle) {
                    if(remainingToEntrance>0 && --remainingToEntrance==0)inLane=true;
                } else if(square==endSquare) {
                    inLane=true;
                }
            }
        }
        return square;
    }

    public static boolean occupiedByFriend(int destination,boolean safeSquare,int[] otherSquares) {
        if(destination<0 || safeSquare || otherSquares==null)return false;
        for(int position:otherSquares)if(position==destination)return true;
        return false;
    }
}
