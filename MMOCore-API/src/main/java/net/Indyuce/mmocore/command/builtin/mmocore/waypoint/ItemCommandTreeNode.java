package net.Indyuce.mmocore.command.builtin.mmocore.waypoint;

import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.util.item.WaypointBookBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ItemCommandTreeNode extends CommandTreeNode {
    public ItemCommandTreeNode(CommandTreeNode parent) {
        super(parent, "item");

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

        new SmartGive(player).give(new WaypointBookBuilder(waypoint).build());
        sender.sendMessage(ChatColor.GOLD + "Gave " + player.getName() + ChatColor.YELLOW + " a waypoint book of " + ChatColor.GOLD + waypoint.getId()
                + ChatColor.YELLOW + ".");
        return CommandResult.SUCCESS;
    }
}
