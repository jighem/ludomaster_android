package com.ludomaster.reference;

/** Presentation state only; the existing BomberRules and turn logic own planting. */
public enum BombControlState {
    HIDDEN, IDLE, ARMED, UNAVAILABLE, ARMED_LOCKED;

    public static BombControlState resolve(boolean bomber,boolean ending,boolean rolling,
            boolean numberVisible,boolean beforeRoll,boolean armed,
            boolean eligibleDeparture,boolean waitingForMove) {
        if(!bomber || ending || rolling || numberVisible)return HIDDEN;
        if(beforeRoll) {
            if(!eligibleDeparture)return UNAVAILABLE;
            return armed?ARMED:IDLE;
        }
        return armed && waitingForMove?ARMED_LOCKED:HIDDEN;
    }
    public boolean canToggle(boolean bot) {
        return !bot && (this==IDLE || this==ARMED);
    }
    public boolean lit() { return this==ARMED || this==ARMED_LOCKED; }
}
