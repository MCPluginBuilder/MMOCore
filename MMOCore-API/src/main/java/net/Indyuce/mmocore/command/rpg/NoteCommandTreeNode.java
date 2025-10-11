package net.Indyuce.mmocore.command.rpg;

import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoteCommandTreeNode extends CommandTreeNode {
	public NoteCommandTreeNode(CommandTreeNode parent) {
		super(parent, "note");

		addArgument(Argument.PLAYER);
		addArgument(Argument.AMOUNT_INT.withKey("worth"));
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[1] + ".");
			return CommandResult.FAILURE;
		}

		int worth;
		try {
			worth = Integer.parseInt(args[2]);
		} catch (NumberFormatException exception) {
			sender.sendMessage(ChatColor.RED + args[2] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		new SmartGive(player).give(new CurrencyItemBuilder("NOTE", worth).build());
		return CommandResult.SUCCESS;
	}
}
