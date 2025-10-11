package net.Indyuce.mmocore.command.rpg.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCommandTreeNode extends CommandTreeNode {
	public OpenCommandTreeNode(CommandTreeNode parent) {
		super(parent, "open");

		addArgument(Argument.PLAYER);
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player " + args[2]);
			return CommandResult.FAILURE;
		}

		InventoryManager.WAYPOINTS.newInventory(PlayerData.get(player)).open();
		return CommandResult.SUCCESS;
	}
}
