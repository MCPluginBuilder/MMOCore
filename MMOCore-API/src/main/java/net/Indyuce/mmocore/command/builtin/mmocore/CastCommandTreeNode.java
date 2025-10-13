package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.cast.FirstCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.cast.SpecificCommandTreeNode;

public class CastCommandTreeNode extends CommandTreeNode {
    public CastCommandTreeNode(CommandTreeNode parent) {
        super(parent, "cast");

        addChild(new FirstCommandTreeNode(this));
        addChild(new SpecificCommandTreeNode(this));
    }
}
