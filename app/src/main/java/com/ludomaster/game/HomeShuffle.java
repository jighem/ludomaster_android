package com.ludomaster.game;

import java.util.Random;

/**
 * Home assignments for a four-quadrant board, deliberately separated from token
 * physical track coordinates. Future logic must derive start/finish from this
 * permutation at the move-resolution boundary.
 */
public final class HomeShuffle {
    public static final int RED = 0, GREEN = 1, YELLOW = 2, BLUE = 3;
    private final int[] colorAtHome = { RED, GREEN, YELLOW, BLUE };
    public int colorAt(int homeIndex) { return colorAtHome[homeIndex]; }
    public int homeOfColor(int color) {
        for (int i = 0; i < 4; i++) if (colorAtHome[i] == color) return i;
        throw new IllegalArgumentException("Unrecognized color");
    }
    /** Fisher-Yates shuffled home labels; no pawn physical location is modified. */
    public void shuffle(Random random) {
        for (int i = colorAtHome.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = colorAtHome[i]; colorAtHome[i] = colorAtHome[j]; colorAtHome[j] = tmp;
        }
    }
    /** Count forward steps along the 52-square circular track, never backward. */
    public static int forwardDistance(int currentTrackIndex, int newEntryTrackIndex) {
        if (currentTrackIndex < 0 || currentTrackIndex >= 52 || newEntryTrackIndex < 0 || newEntryTrackIndex >= 52)
            throw new IllegalArgumentException("Track index must be 0..51");
        return (newEntryTrackIndex - currentTrackIndex + 52) % 52;
    }
}
