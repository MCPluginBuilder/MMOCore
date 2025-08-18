package net.Indyuce.mmocore.command.rpg.quest;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class QuestCommandTreeNode extends CommandTreeNode {

	public QuestCommandTreeNode(CommandTreeNode parent) {
		super(parent, "quest");

		addChild(new StartCommandTreeNode(this));
		addChild(new CancelCommandTreeNode(this));
		addChild(new FinishCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
