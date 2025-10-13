package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class BoosterCommandTreeNode extends CommandTreeNode {
	public BoosterCommandTreeNode(CommandTreeNode parent) {
		super(parent, "booster");

		addChild(new CreateCommandTreeNode(this));
		addChild(new ListCommandTreeNode(this));
		addChild(new RemoveCommandTreeNode(this));
	}
}
