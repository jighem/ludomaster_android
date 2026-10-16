package com.ludomaster.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * First playable offline, pass-and-play Classic engine.
 * Four pawns per active color; physical board coordinates from track progress.
 * Optional combat, immunity and forward-only dynamic home shuffle.
 */
public final class ClassicGame {
    public static final int[][] TRACK = {
        {6,1},{6,2},{6,3},{6,4},{6,5},{5,6},{4,6},{3,6},{2,6},{1,6},{0,6},{0,7},{0,8},
        {1,8},{2,8},{3,8},{4,8},{5,8},{6,9},{6,10},{6,11},{6,12},{6,13},{6,14},{7,14},{8,14},
        {8,13},{8,12},{8,11},{8,10},{8,9},{9,8},{10,8},{11,8},{12,8},{13,8},{14,8},{14,7},{14,6},
        {13,6},{12,6},{11,6},{10,6},{9,6},{8,5},{8,4},{8,3},{8,2},{8,1},{8,0},{7,0},{6,0}
    };
    public static final int[][][] LANES = {
        {{7,1},{7,2},{7,3},{7,4},{7,5},{7,6}},
        {{1,7},{2,7},{3,7},{4,7},{5,7},{6,7}},
        {{7,13},{7,12},{7,11},{7,10},{7,9},{7,8}},
        {{13,7},{12,7},{11,7},{10,7},{9,7},{8,7}}
    };
    public static final int[] START = {0,13,26,39};
    public static final int[] COLORS = {0xffe53935,0xff43a047,0xfffdd835,0xff1e88e5};
    public static final String[] NAMES = {"Red","Green","Yellow","Blue"};
    public static final int[] SAFE = {0,8,13,21,26,34,39,47};
    private final SecureRandom random = new SecureRandom();
    private final java.util.Random shuffleRandom = new SecureRandom();
    /** Quadrant (0..3) currently assigned to each colour. */
    public final int[] homeQuadrant = {0,1,2,3};
    public int shuffleCount = 0;
    private boolean shufflePending = false;
    public final MatchOptions options;
    public final CombatLedger[] ledgers = new CombatLedger[4];
    public int pendingDefender = -1, pendingDefenderPawn = -1;
    private final java.util.ArrayList<int[]> pendingVictims = new java.util.ArrayList<>();
    private boolean moveGrantedExtraTurn;
    private String lastMoveDescription = "";
    public final int[][] progress = new int[4][4]; // -1 yard, 0..51 track, 52..57 finish lane
    public final int[] captures = new int[4];
    public final int[] deaths = new int[4];
    public final boolean[] active = new boolean[4];
    public int turn = 0, die = 0, consecutiveSixes = 0, winner = -1;
    public boolean awaitingPawn = false;
    public String message = "Roll the dice to begin";

    public ClassicGame(int count) { this(count, new MatchOptions(false, false, false)); }
    public ClassicGame(int count, MatchOptions options) {
        this.options = options;
        for (int c = 0; c < 4; c++) ledgers[c] = new CombatLedger(options);
        if (count < 2 || count > 4) throw new IllegalArgumentException("Choose 2–4 players");
        active[0] = true; active[2] = true;
        if (count >= 3) active[1] = true;
        if (count == 4) active[3] = true;
        for (int c = 0; c < 4; c++) for (int p = 0; p < 4; p++) progress[c][p] = -1;
    }
    public int[] position(int color, int pawn) {
        int p = progress[color][pawn];
        if (p < 0) {
            int[][] yards = {{1,1},{1,10},{10,10},{10,1}};
            int home = homeQuadrant[color];
            return new int[] { yards[home][0] + (pawn/2)*3, yards[home][1] + (pawn%2)*3 };
        }
        if (p >= 52) return LANES[homeQuadrant[color]][p-52].clone();
        return TRACK[(START[homeQuadrant[color]]+p)%52].clone();
    }
    public boolean canMove(int pawn) {
        if (winner != -1 || !awaitingPawn || pendingDefender != -1 || pawn < 0 || pawn >= 4) return false;
        int p = progress[turn][pawn];
        return p == -1 ? die == 6 : p < 57 && p + die <= 57;
    }
    public List<Integer> legalPawns() {
        List<Integer> out = new ArrayList<>();
        for (int p=0;p<4;p++) if (canMove(p)) out.add(p);
        return out;
    }
    public void roll() {
        if (winner != -1 || awaitingPawn || pendingDefender != -1) return;
        die = random.nextInt(6) + 1;
        if (die == 6) consecutiveSixes++; else consecutiveSixes = 0;
        if (consecutiveSixes == 3) {
            message = NAMES[turn] + " rolled three consecutive sixes: turn forfeited.";
            consecutiveSixes = 0; die = 0; nextTurn(); return;
        }
        awaitingPawn = true;
        if (legalPawns().isEmpty()) {
            message = NAMES[turn] + " rolled " + die + " — no legal moves.";
            awaitingPawn = false; die = 0; nextTurn();
        } else {
            message = NAMES[turn] + " rolled " + die + ". Tap one of your movable pawns.";
        }
    }
    public boolean move(int pawn) {
        if (!canMove(pawn)) return false;
        int old = progress[turn][pawn];
        progress[turn][pawn] = old == -1 ? 0 : old + die;
        int now = progress[turn][pawn];
        pendingVictims.clear();
        if (now >= 0 && now < 52) {
            int cell = (START[homeQuadrant[turn]] + now) % 52;
            if (!safe(cell)) {
                for (int c = 0; c < 4; c++) if (c != turn && active[c]) {
                    for (int p = 0; p < 4; p++) {
                        int other = progress[c][p];
                        if (other >= 0 && other < 52 && (START[homeQuadrant[c]] + other) % 52 == cell)
                            pendingVictims.add(new int[] {c,p});
                    }
                }
            }
        }
        moveGrantedExtraTurn = die == 6 || now == 57;
        lastMoveDescription = NAMES[turn] + " moved pawn " + (pawn + 1);
        awaitingPawn = false;
        die = 0;
        resolveNextVictim();
        return true;
    }

    /** Resolve one defender decision at a time, so shields cannot be bypassed. */
    private void resolveNextVictim() {
        while (!pendingVictims.isEmpty()) {
            int[] victim = pendingVictims.remove(0);
            int color = victim[0], pawn = victim[1];
            if (options.immunity && ledgers[color].shields() > 0) {
                pendingDefender = color;
                pendingDefenderPawn = pawn;
                message = NAMES[color] + " can spend an immunity shield to avoid capture.";
                return;
            }
            applyCapture(color, pawn);
        }
        finishMove();
    }

    public void decideShield(boolean useShield) {
        if (pendingDefender == -1) return;
        int color = pendingDefender, pawn = pendingDefenderPawn;
        pendingDefender = -1;
        pendingDefenderPawn = -1;
        if (!useShield || !ledgers[color].spendShield())
            applyCapture(color, pawn);
        else
            message = NAMES[color] + " blocked a capture with immunity.";
        resolveNextVictim();
    }

    private void applyCapture(int color, int pawn) {
        progress[color][pawn] = -1;
        captures[turn]++;
        deaths[color]++;
        ledgers[turn].recordCapture();
        ledgers[color].recordDeath();
        moveGrantedExtraTurn = true;
        lastMoveDescription += " and captured " + NAMES[color];
    }

    private void finishMove() {
        boolean allFinished = true;
        for (int p = 0; p < 4; p++) if (progress[turn][p] != 57) allFinished = false;
        if (allFinished) {
            winner = turn;
            message = NAMES[turn] + " wins!";
            applyPendingShuffle();
            return;
        }
        message = lastMoveDescription + ".";
        if (!moveGrantedExtraTurn) {
            consecutiveSixes = 0;
            nextTurn();
        }
        applyPendingShuffle();
    }
    /** Timer signals may arrive mid-move; apply only after move/capture resolution. */
    public void requestHomeShuffle() {
        if (!options.dynamicHomeShuffle || winner != -1) return;
        shufflePending = true;
        if (!awaitingPawn && pendingDefender == -1 && pendingVictims.isEmpty()) applyPendingShuffle();
    }
    private void applyPendingShuffle() {
        if (!shufflePending || pendingDefender != -1 || !pendingVictims.isEmpty() || awaitingPawn) return;
        shufflePending = false;
        // Shuffle by permuting colour->quadrant; always produce a changed arrangement.
        int[] previous = homeQuadrant.clone();
        for (int i=3;i>0;i--) {
            int j=shuffleRandom.nextInt(i+1);
            int t=homeQuadrant[i];homeQuadrant[i]=homeQuadrant[j];homeQuadrant[j]=t;
        }
        if (java.util.Arrays.equals(previous,homeQuadrant)) {
            int t=homeQuadrant[0];homeQuadrant[0]=homeQuadrant[1];homeQuadrant[1]=t;
        }
        for(int c=0;c<4;c++) for(int p=0;p<4;p++) {
            int old=progress[c][p];
            if(old>=0 && old<52) {
                // Re-express the same physical track cell relative to the new starting square.
                int absolute=(START[previous[c]]+old)%52;
                progress[c][p]=(absolute-START[homeQuadrant[c]]+52)%52;
            }
            // Tokens already in a finishing lane keep their lane depth and move
            // visually with the assigned colour's NEW finishing lane.
            // Completed tokens remain completed.
        }
        shuffleCount++;
        message="Home shuffle #"+shuffleCount+"! Starting squares and entrances changed.";
    }
    public int colorAtQuadrant(int quadrant) {
        for(int c=0;c<4;c++) if(homeQuadrant[c]==quadrant) return c;
        throw new IllegalArgumentException("Invalid quadrant");
    }
    public int combatPoints(int color) { return ledgers[color].combatPoints(); }
    public int shields(int color) { return ledgers[color].shields(); }
    private static boolean safe(int cell) {
        for (int s:SAFE) if (s==cell) return true;
        return false;
    }
    private void nextTurn() {
        do { turn=(turn+1)%4; } while (!active[turn]);
        die=0;awaitingPawn=false;
    }
}
