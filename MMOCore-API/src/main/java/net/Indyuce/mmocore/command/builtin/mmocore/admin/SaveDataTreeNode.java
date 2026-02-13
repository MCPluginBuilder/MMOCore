package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Saves player data
 */
public class SaveDataTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;

    public SaveDataTreeNode(CommandTreeNode parent) {
        super(parent, "savedata");

        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);

        MMOCore.plugin.playerDataManager.saveData(PlayerData.get(player), SessionUpdateReason.AUTOSAVE);

        return CommandResult.SUCCESS;
    }
}
