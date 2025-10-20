package net.Indyuce.mmocore.command.builtin.mmocore.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LockCommandTreeNode extends CommandTreeNode {

    public LockCommandTreeNode(CommandTreeNode parent) {
        super(parent, "lock");

        addArgument(Arguments.WAYPOINT);
        addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 4)
            return CommandResult.THROW_USAGE;

        Waypoint waypoint = MMOCore.plugin.waypointManager.get(args[2]);
        if (waypoint == null) {
            sender.sendMessage(ChatColor.RED + "Could not find waypoint " + args[2]);
            return CommandResult.FAILURE;
        }

        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find player " + args[3]);
            return CommandResult.FAILURE;
        }
        PlayerData playerData = PlayerData.get(player);

        if (!playerData.hasWaypoint(waypoint)) {
            sender.sendMessage(ChatColor.RED + "The waypoint " + args[2] + " is already locked.");
            return CommandResult.FAILURE;
        }
        PlayerData.get(player).lockWaypoint(waypoint);
        return explorer.success(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " successfully locked " + ChatColor.GOLD + waypoint.getId());

    }
}
