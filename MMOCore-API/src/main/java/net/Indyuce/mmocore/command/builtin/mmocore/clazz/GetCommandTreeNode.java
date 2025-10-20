package net.Indyuce.mmocore.command.builtin.mmocore.clazz;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;

    public GetCommandTreeNode(CommandTreeNode parent) {
        super(parent, "get");

        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var playerData = PlayerData.get(player);

        return explorer.success("Player &6" + player.getName() + "&e currently has class &6" + playerData.getProfess().getName());
    }
}
