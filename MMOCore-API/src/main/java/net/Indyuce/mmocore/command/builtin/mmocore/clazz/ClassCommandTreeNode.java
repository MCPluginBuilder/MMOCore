package net.Indyuce.mmocore.command.builtin.mmocore.clazz;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class ClassCommandTreeNode extends CommandTreeNode {

    public ClassCommandTreeNode(CommandTreeNode parent) {
        super(parent, "class");

        addChild(new GetCommandTreeNode(this));
        addChild(new SelectCommandTreeNode(this));
        addChild(new ForceCommandTreeNode(this));
    }
}