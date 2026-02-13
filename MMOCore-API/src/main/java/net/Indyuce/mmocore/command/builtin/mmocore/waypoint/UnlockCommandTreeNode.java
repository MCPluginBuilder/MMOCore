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

public class UnlockCommandTreeNode extends CommandTreeNode {
    private final Argument<Waypoint> argWaypoint;
    private final Argument<Player> argPlayer;

    public UnlockCommandTreeNode(CommandTreeNode parent) {
        super(parent, "unlock");

        argWaypoint = addArgument(Arguments.WAYPOINT);
        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        var waypoint = explorer.parse(argWaypoint);
        var player = explorer.parse(argPlayer);

        PlayerData.get(player).unlockWaypoint(waypoint);
        return explorer.success(player.getName() + "&e successfully unlocked &6" + waypoint.getId());
    }
}
