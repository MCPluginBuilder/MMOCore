package net.Indyuce.mmocore.command.builtin.mmocore.skill;

import io.lumine.mythic.lib.command.CommandTreeNode;


public class SkillCommandTreeNode extends CommandTreeNode {
    public SkillCommandTreeNode(CommandTreeNode parent) {
        super(parent, "skill");

        addChild(new LockCommandTreeNode(this, "lock"));
        addChild(new UnlockCommandTreeNode(this, "unlock"));
        addChild(new LevelCommandTreeNode(this));
    }
}
