package net.Indyuce.mmocore.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * State of one skill tree node, or path between nodes.
 *
 * @see PlayerData#getNodeState(SkillTreeNode) 
 * @see SkillTreePath#getStatus(PlayerData)
 */
public enum NodeState {

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
     * No more skill points can be spent on the node since it has already
     * been maxed out. Technically it is a subtype of UNLOCKED since you can't
     * be MAXED_OUT without being UNLOCKED.
     */
    MAXED_OUT,

    /**
     * The player made a choice making it now impossible to reach this
     * node given its skill tree exploration. The player needs to
     * re-spec to unlock that node.
     */
    FULLY_LOCKED;

    public boolean isUnlocked() {
        return this == UNLOCKED || this == MAXED_OUT;
    }
}
