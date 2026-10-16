package com.ludomaster.game;

/** Pure per-player prototype for optional combat points and immunity. */
public final class CombatLedger {
    private int kills;
    private int deaths;
    private int shields;
    private final MatchOptions options;
    public CombatLedger(MatchOptions options) { this.options = options; }
    public void recordCapture() { kills++; if (options.immunity) shields++; }
    /** Returns true if defender is protected and attacker cannot capture. */
    public boolean spendShield() {
        if (!options.immunity || shields <= 0) return false;
        shields--;
        return true;
    }
    public void recordDeath() { deaths++; }
    public int combatPoints() { return options.combatScoring ? (kills - deaths) * 5 : 0; }
    public int kills() { return kills; }
    public int deaths() { return deaths; }
    public int shields() { return shields; }
}
