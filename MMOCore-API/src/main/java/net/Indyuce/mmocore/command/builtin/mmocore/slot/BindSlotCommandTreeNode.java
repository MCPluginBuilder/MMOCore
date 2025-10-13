package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindSlotCommandTreeNode extends CommandTreeNode {

    public BindSlotCommandTreeNode(CommandTreeNode parent, String id) {
        super(parent, id);

        addArgument(Argument.PLAYER);
        addArgument(Arguments.INDEX);
        addArgument(Arguments.SKILL);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 6)
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
        ClassSkill skill = playerData.getProfess().getSkill(args[5]);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "The player's class doesn't have a skill called  " + args[5] + ".");
            return CommandResult.FAILURE;
        }
        playerData.bindSkill(slot, skill);

        return explorer.success("Skill &6" + skill.getSkill().getHandler().getId() + "&e now bound to slot &6" + slot);
    }
}