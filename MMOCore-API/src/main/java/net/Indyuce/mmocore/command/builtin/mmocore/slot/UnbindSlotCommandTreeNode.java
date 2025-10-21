package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbindSlotCommandTreeNode extends CommandTreeNode {

    public UnbindSlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "unbind");

        addArgument(Argument.PLAYER);
        addArgument(Argument.AMOUNT_INT);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 5)
            return CommandResult.THROW_USAGE;
        Player player = Bukkit.getPlayer(args[3]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
            return CommandResult.FAILURE;
        }
        PlayerData playerData = PlayerData.get(player);
        int slot;
        try {
            slot = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        final BoundSkillInfo found = playerData.unbindSkill(slot);
        return explorer.success((found != null ?
                "Skill " + ChatColor.GOLD + found.getClassSkill().getSkill().getName() + ChatColor.YELLOW + " was taken off the slot " + ChatColor.GOLD + slot :
                "Could not find skill at slot " + ChatColor.GOLD + slot));
    }
}