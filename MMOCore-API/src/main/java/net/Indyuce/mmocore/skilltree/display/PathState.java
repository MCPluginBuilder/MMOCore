package net.Indyuce.mmocore.skilltree.display;

public enum PathState {

    /**
     * The player has purchased and unlocked the skill tree node.
     */
    UNLOCKED,

    /**
     * The player has instant access to but has not unlocked the node.
     */
    UNLOCKABLE,

    /**
     * The player does not have access to this skill node but it
     * remains a possibility to access it.
     */
    LOCKED,

    /**
     * The player made a choice making it now impossible to reach this
     * node given its skill tree exploration. The player needs to
     * re-spec to unlock that node.
     */
    FULLY_LOCKED
}
