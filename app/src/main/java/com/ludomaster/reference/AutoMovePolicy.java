package com.ludomaster.reference;

import java.util.List;
import java.util.Objects;

/** Automate a forced move or interchangeable pawns, while preserving meaningful choices. */
public final class AutoMovePolicy {
    private AutoMovePolicy() { }
    public static final class Choice {
        public final int source,destination,bombsLeft;
        public final boolean inHome;
        public final String routeState;
        public Choice(int source,int destination,boolean inHome,int bombsLeft,String routeState) {
            this.source=source;this.destination=destination;this.inHome=inHome;
            this.bombsLeft=bombsLeft;this.routeState=routeState;
        }
        private boolean equivalent(Choice other) {
            return other!=null && source==other.source && destination==other.destination &&
                inHome==other.inHome && bombsLeft==other.bombsLeft &&
                Objects.equals(routeState,other.routeState);
        }
    }
    /** Caller supplies only legal, currently selectable moves. */
    public static int automaticIndex(List<Choice> legalChoices) {
        if(legalChoices==null || legalChoices.isEmpty() || legalChoices.get(0)==null)return -1;
        Choice first=legalChoices.get(0);
        for(Choice choice:legalChoices)if(!first.equivalent(choice))return -1;
        return 0;
    }
}
