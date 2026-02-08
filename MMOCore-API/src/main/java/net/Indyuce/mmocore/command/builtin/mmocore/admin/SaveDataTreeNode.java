package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Saves player data
 */
public class SaveDataTreeNode extends CommandTreeNode {
    public SaveDataTreeNode(CommandTreeNode parent) {
        super(parent, "savedata");

        addArgument(Argument.PLAYER);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
            return CommandResult.FAILURE;
        }

        MMOCore.plugin.playerDataManager.saveData(PlayerData.get(player), SessionUpdateReason.AUTOSAVE);

        return CommandResult.SUCCESS;
    }
}
