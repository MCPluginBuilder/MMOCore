package net.Indyuce.mmocore.command.rpg;

import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.command.rpg.cast.FirstCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.cast.SpecificCommandTreeNode;

public class CastCommandTreeNode extends CommandTreeNode {
    public CastCommandTreeNode(CommandTreeNode parent) {
        super(parent, "cast");

        addChild(new FirstCommandTreeNode(this));
        addChild(new SpecificCommandTreeNode(this));
    }
}
