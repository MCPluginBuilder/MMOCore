package net.Indyuce.mmocore.command.rpg.waypoint;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnlockCommandTreeNode extends CommandTreeNode {
	public UnlockCommandTreeNode(CommandTreeNode parent) {
		super(parent, "unlock");

		addParameter(new Parameter("<waypoint>", (explorer, list) -> MMOCore.plugin.waypointManager.getAll().forEach(way -> list.add(way.getId()))));
		addParameter(Parameter.PLAYER);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
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
