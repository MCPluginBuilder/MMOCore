package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.message.actionbar.ActionBarPriority;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HideActionBarCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Long> argDuration;

    public HideActionBarCommandTreeNode(CommandTreeNode parent) {
        super(parent, "hideab");

        argPlayer = addArgument(Argument.PLAYER);
        argDuration = addArgument(Argument.DURATION_TICKS);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var amount = explorer.parse(argDuration);

        PlayerData.get(player).getMMOPlayerData().getActionBar().hide(ActionBarPriority.LOW, amount);
        return CommandResult.SUCCESS;
    }
}
