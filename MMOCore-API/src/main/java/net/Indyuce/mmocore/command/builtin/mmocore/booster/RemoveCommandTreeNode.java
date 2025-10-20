package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.Booster;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.UUID;

public class RemoveCommandTreeNode extends CommandTreeNode {
	public RemoveCommandTreeNode(CommandTreeNode parent) {
		super(parent, "remove");

		addArgument(Arguments.BOOSTER);
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		UUID uuid;
		try {
			uuid = UUID.fromString(args[2]);
		} catch (IllegalArgumentException exception) {
			sender.sendMessage(ChatColor.RED + "Couldn't load ID " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		for (Iterator<Booster> iterator = MMOCore.plugin.boosterManager.getActive().iterator(); iterator.hasNext();) {
			Booster booster = iterator.next();
			if (booster.getUniqueId().equals(uuid)) {
				iterator.remove();
				sender.sendMessage(ChatColor.YELLOW + "Successfully unregistered this booster.");
				break;
			}
		}
		return CommandResult.SUCCESS;
	}
}
