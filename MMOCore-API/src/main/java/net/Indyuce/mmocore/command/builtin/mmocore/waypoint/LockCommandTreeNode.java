package net.Indyuce.mmocore.command.builtin.mmocore.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LockCommandTreeNode extends CommandTreeNode {
    private final Argument<Waypoint> argWaypoint;
    private final Argument<Player> argPlayer;

    public LockCommandTreeNode(CommandTreeNode parent) {
        super(parent, "lock");

        argWaypoint = addArgument(Arguments.WAYPOINT);
        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Waypoint waypoint = explorer.parse(argWaypoint);
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);

        if (!playerData.hasWaypoint(waypoint))
            return explorer.fail(player.getName() + " doesn't have the waypoint " + waypoint.getId());

        PlayerData.get(player).lockWaypoint(waypoint);
        return explorer.success("&6" + player.getName() + "&e successfully locked &6" + waypoint.getId());
    }
}
