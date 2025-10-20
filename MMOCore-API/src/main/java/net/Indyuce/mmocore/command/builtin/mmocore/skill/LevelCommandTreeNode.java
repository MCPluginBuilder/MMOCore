package net.Indyuce.mmocore.command.builtin.mmocore.skill;

import io.lumine.mythic.lib.command.CommandTreeNode;


public class LevelCommandTreeNode extends CommandTreeNode {
    public LevelCommandTreeNode(CommandTreeNode parent) {
        super(parent, "level");

        addChild(new EditLevelCommandTreeNode(this, "give", Integer::sum));
        addChild(new EditLevelCommandTreeNode(this, "set", (old, amount) -> amount));
    }
}
