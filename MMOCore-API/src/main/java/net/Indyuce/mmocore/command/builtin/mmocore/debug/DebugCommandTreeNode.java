package net.Indyuce.mmocore.command.builtin.mmocore.debug;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class DebugCommandTreeNode extends CommandTreeNode {
	public DebugCommandTreeNode(CommandTreeNode parent) {
		super(parent, "debug");

		addChild(new StatValueCommandTreeNode(this));
		addChild(new StatModifiersCommandTreeNode(this));
	}
}
