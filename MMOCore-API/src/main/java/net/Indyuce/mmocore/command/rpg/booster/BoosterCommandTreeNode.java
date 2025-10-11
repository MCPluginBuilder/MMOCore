package net.Indyuce.mmocore.command.rpg.booster;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class BoosterCommandTreeNode extends CommandTreeNode {
	public BoosterCommandTreeNode(CommandTreeNode parent) {
		super(parent, "booster");

		addChild(new CreateCommandTreeNode(this));
		addChild(new ListCommandTreeNode(this));
		addChild(new RemoveCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
