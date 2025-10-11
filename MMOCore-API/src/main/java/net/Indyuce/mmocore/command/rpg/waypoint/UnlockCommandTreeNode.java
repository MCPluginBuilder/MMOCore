package net.Indyuce.mmocore.command.rpg.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UnlockCommandTreeNode extends CommandTreeNode {
	public UnlockCommandTreeNode(CommandTreeNode parent) {
		super(parent, "unlock");

		addArgument(Arguments.WAYPOINT);
		addArgument(Argument.PLAYER);
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		var waypoint = MMOCore.plugin.waypointManager.get(args[2]);
		if (waypoint == null) {
			sender.sendMessage(ChatColor.RED + "Could not find waypoint " + args[2]);
			return CommandResult.FAILURE;
		}

		var player = Bukkit.getPlayer(args[3]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player " + args[3]);
			return CommandResult.FAILURE;
		}

		PlayerData.get(player).unlockWaypoint(waypoint);
		sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " successfully unlocked " + ChatColor.GOLD + waypoint.getId()
				+ ChatColor.YELLOW + ".");
		return CommandResult.SUCCESS;
	}
}
