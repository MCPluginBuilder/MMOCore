package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.message.actionbar.ActionBarPriority;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideActionBarCommandTreeNode extends CommandTreeNode {
    public HideActionBarCommandTreeNode(CommandTreeNode parent) {
        super(parent, "hideab");

        addArgument(Argument.PLAYER);
        addArgument(Argument.DURATION_TICKS);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 4)
            return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
            return CommandResult.FAILURE;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        PlayerData.get(player).getMMOPlayerData().getActionBar().hide(ActionBarPriority.LOW, amount);
        return CommandResult.SUCCESS;
    }
}
