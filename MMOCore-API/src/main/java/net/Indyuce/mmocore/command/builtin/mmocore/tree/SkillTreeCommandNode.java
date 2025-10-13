package net.Indyuce.mmocore.command.builtin.mmocore.tree;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class SkillTreeCommandNode extends CommandTreeNode {
    public SkillTreeCommandNode(CommandTreeNode parent) {
        super(parent, "tree");

        addChild(new OpenCommandNode(this));
    }
}
