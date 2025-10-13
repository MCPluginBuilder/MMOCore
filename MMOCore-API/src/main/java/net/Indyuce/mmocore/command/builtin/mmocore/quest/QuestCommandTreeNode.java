package net.Indyuce.mmocore.command.builtin.mmocore.quest;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class QuestCommandTreeNode extends CommandTreeNode {

	public QuestCommandTreeNode(CommandTreeNode parent) {
		super(parent, "quest");

		addChild(new StartCommandTreeNode(this));
		addChild(new CancelCommandTreeNode(this));
		addChild(new FinishCommandTreeNode(this));
	}
}
