package com.ludomaster.reference.shuffle;

/** Pure state model; legacy Piece adapter and UI binding are intentionally separate. */
public final class ShufflePawn {
    public enum Zone { YARD, TRACK, LANE, COMPLETED }
    public final Zone zone;
    public final int position;
    // Number of physical squares until the newly assigned entrance can be reached
    // by moving forward. 0 means standing on the entrance and requires another lap.
    public final int stepsToEntrance;

    private ShufflePawn(Zone zone, int position, int stepsToEntrance) {
        this.zone=zone; this.position=position; this.stepsToEntrance=stepsToEntrance;
    }
    public static ShufflePawn yard() { return new ShufflePawn(Zone.YARD,-1,-1); }
    public static ShufflePawn track(int square,int entrance) {
        int distance=HomeShuffleEngine.forwardDistance(square,entrance);
        return new ShufflePawn(Zone.TRACK,square,distance==0?52:distance);
    }
    public static ShufflePawn lane(int step) {
        if(step<0||step>5)throw new IllegalArgumentException("lane");
        return new ShufflePawn(Zone.LANE,step,-1);
    }
    public static ShufflePawn completed() {return new ShufflePawn(Zone.COMPLETED,-1,-1);}
    public ShufflePawn remap(int newEntrance) {
        if(zone!=Zone.TRACK)return this;
        return track(position,newEntrance);
    }
    public ShufflePawn advanceTrack(int roll) {
        if(zone!=Zone.TRACK||roll<1||roll>6)throw new IllegalArgumentException("track roll");
        if(roll>stepsToEntrance)throw new IllegalStateException("roll crosses entrance; route to lane separately");
        return new ShufflePawn(Zone.TRACK,HomeShuffleEngine.forwardDistance(0,(position+roll)%52),stepsToEntrance-roll);
    }
}
