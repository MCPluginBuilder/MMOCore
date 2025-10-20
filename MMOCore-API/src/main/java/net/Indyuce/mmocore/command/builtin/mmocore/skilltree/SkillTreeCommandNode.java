package net.Indyuce.mmocore.command.builtin.mmocore.skilltree;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class SkillTreeCommandNode extends CommandTreeNode {
    public SkillTreeCommandNode(CommandTreeNode parent) {
        super(parent, "skill-tree");

        addChild(new OpenCommandNode(this));
    }
}
