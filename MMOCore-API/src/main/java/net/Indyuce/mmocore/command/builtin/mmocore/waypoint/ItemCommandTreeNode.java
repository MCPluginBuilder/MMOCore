package net.Indyuce.mmocore.command.builtin.mmocore.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.SmartGive;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.util.item.WaypointBookBuilder;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemCommandTreeNode extends CommandTreeNode {
    private final Argument<Waypoint> argWaypoint;
    private final Argument<Player> argPlayer;

    public ItemCommandTreeNode(CommandTreeNode parent) {
        super(parent, "item");

        argWaypoint = addArgument(Arguments.WAYPOINT);
        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        var waypoint = explorer.parse(argWaypoint);
        var player = explorer.parse(argPlayer);

        new SmartGive(player).give(new WaypointBookBuilder(waypoint).build());
        return explorer.success("Gave " + player.getName() + "&e a waypoint book of &6" + waypoint.getId());
    }
}
