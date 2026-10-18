package com.ludomaster.reference.shuffle;

import java.util.Random;

/**
 * Pure logical model for the four fixed physical home slots.
 * Does not mutate Android View coordinates or the legacy game engine.
 */
public final class HomeShuffleEngine {
    public static final int TRACK_LENGTH = 52;
    public static final int[] START = {0, 13, 26, 39};
    // Legacy Piece constructor computes startPosition != 0 ? startPosition - 2 : 50.
    public static final int[] ENTRANCE = {50, 11, 24, 37};
    public static final int[] LANE_START = {52, 58, 64, 70};
    private final Random random;
    private int[] colourToSlot = {0, 1, 2, 3};
    private boolean pending;

    public HomeShuffleEngine(long seed) { random = new Random(seed); }
    public int homeSlot(int colour) { checkColour(colour); return colourToSlot[colour]; }
    public int startSquare(int colour) { return START[homeSlot(colour)]; }
    public int entranceSquare(int colour) { return ENTRANCE[homeSlot(colour)]; }
    public int laneSquare(int colour, int laneStep) {
        if (laneStep < 0 || laneStep >= 6) throw new IllegalArgumentException("lane step");
        return LANE_START[homeSlot(colour)] + laneStep;
    }
    public int[] assignment() { return colourToSlot.clone(); }

    /** Validate and restore a persisted home permutation. */
    public void restoreAssignment(int[] saved) {
        if (saved == null || saved.length != 4) throw new IllegalArgumentException("assignment length");
        boolean[] seen = new boolean[4];
        for (int slot : saved) {
            if (slot < 0 || slot > 3 || seen[slot]) throw new IllegalArgumentException("invalid permutation");
            seen[slot] = true;
        }
        colourToSlot = saved.clone();
        pending = false;
    }

    /** Always move on the physical track, independent of colour assignment. */
    public static int moveOnTrack(int current, int steps) {
        checkTrack(current);
        if (steps < 0 || steps > 6) throw new IllegalArgumentException("steps");
        return (current + steps) % TRACK_LENGTH;
    }


    /** Timer requests a shuffle; consumer must call applyAtSafeBoundary after full move resolution. */
    public void requestShuffle() { pending = true; }
    public boolean isPending() { return pending; }
    public boolean applyAtSafeBoundary(boolean moveOrShieldResolutionInProgress) {
        if (!pending || moveOrShieldResolutionInProgress) return false;
        int[] next = colourToSlot.clone();
        boolean valid = false;
        for (int attempt = 0; attempt < 64; attempt++) {
            for (int i = 3; i > 0; i--) {
                int j = random.nextInt(i+1);
                int t = next[i]; next[i] = next[j]; next[j] = t;
            }
            valid = true;
            for (int i = 0; i < 4; i++) if (next[i] == colourToSlot[i]) valid = false;
            if (valid) break;
        }
        if (!valid) {
            next = colourToSlot.clone();
            int last = next[3];
            for (int i = 3; i > 0; i--) next[i] = next[i-1];
            next[0] = last;
        }
        colourToSlot = next;
        pending = false;
        return true;
    }

    /** Track pawns retain their physical position; this is a forward-only distance. */
    public static int forwardDistance(int physicalSquare, int targetSquare) {
        checkTrack(physicalSquare); checkTrack(targetSquare);
        return (targetSquare - physicalSquare + TRACK_LENGTH) % TRACK_LENGTH;
    }
    private static void checkColour(int colour) {
        if (colour < 0 || colour > 3) throw new IllegalArgumentException("colour");
    }
    private static void checkTrack(int position) {
        if (position < 0 || position >= TRACK_LENGTH) throw new IllegalArgumentException("physical square");
    }
    public static void validateMinutes(int minutes) {
        if (minutes != 1 && minutes != 2 && minutes != 3 && minutes != 5 && minutes != 10)
            throw new IllegalArgumentException("unsupported interval");
    }
}
