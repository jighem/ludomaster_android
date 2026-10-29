package com.ludomaster.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Match-scoped bomb state. Rendering, turns and animation remain outside this class. */
public final class BomberRules {
    public static final int BOMBS_PER_PAWN=4;
    private final Map<String,Integer> spent=new HashMap<>();
    private final Map<Integer,Bomb> planted=new HashMap<>();
    public static final class Bomb {
        public final int square;
        public final String ownerColor;
        private Bomb(int square,String ownerColor) { this.square=square;this.ownerColor=ownerColor; }
    }
    public static boolean safe(int square) {
        return square==0 || square==8 || square==13 || square==21 ||
            square==26 || square==34 || square==39 || square==47;
    }
    public static boolean plantableSquare(int square) {
        return square>=0 && square<52 && !safe(square);
    }
    public int remaining(String pawnId) {
        return BOMBS_PER_PAWN-spent.getOrDefault(pawnId,0);
    }
    public boolean canPlant(String pawnId,int square,boolean alive,boolean completed) {
        return pawnId!=null && alive && !completed && plantableSquare(square) &&
            remaining(pawnId)>0 && !planted.containsKey(square);
    }
    public boolean plant(String pawnId,String ownerColor,int square,boolean alive,boolean completed) {
        if(ownerColor==null || !canPlant(pawnId,square,alive,completed)) return false;
        spent.put(pawnId,BOMBS_PER_PAWN-remaining(pawnId)+1);
        planted.put(square,new Bomb(square,ownerColor));
        return true;
    }
    public boolean hasBomb(int square) { return planted.containsKey(square); }
    public Map<Integer,Bomb> bombs() { return Collections.unmodifiableMap(new HashMap<>(planted)); }
    /** Called only on landing. Removal makes a bomb single-use even when shielded. */
    public Bomb detonate(int square) { return planted.remove(square); }
    public static String creditedKiller(Bomb bomb,String victimColor) {
        return bomb!=null && !bomb.ownerColor.equals(victimColor) ? bomb.ownerColor : null;
    }
}
