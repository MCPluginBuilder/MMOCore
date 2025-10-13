package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.Booster;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CreateCommandTreeNode extends CommandTreeNode {
    public CreateCommandTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        addArgument(Arguments.PROFESSION);
        addArgument(Argument.AMOUNT_DOUBLE
                .withAutoComplete((explorer, list) -> list.addAll(Arrays.asList("0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1"))));
        addArgument(Argument.DURATION_TICKS);
        addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 5)
            return CommandResult.THROW_USAGE;

        double extra;
        try {
            extra = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        long length;
        try {
            length = Long.parseLong(args[4]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + args[4] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        var multFormatted = MythicLib.plugin.getMMOConfig().decimal.format(1 + extra);
        if (args[2].equalsIgnoreCase("main")) {
            MMOCore.plugin.boosterManager.register(new Booster(args.length > 5 ? args[5] : null, extra, length));
            Message.NEW_EXP_BOOSTER_MAIN.send(Bukkit.getOnlinePlayers(), "multiplier", multFormatted);
            return CommandResult.SUCCESS;
        }

        String format = args[2].toLowerCase().replace("_", "-");
        if (!MMOCore.plugin.professionManager.has(format)) {
            sender.sendMessage(ChatColor.RED + format + " is not a valid profession.");
            return CommandResult.FAILURE;
        }

        var profession = MMOCore.plugin.professionManager.get(format);
        MMOCore.plugin.boosterManager.register(new Booster(args.length > 5 ? args[5] : null, profession, extra, length));
        Message.NEW_EXP_BOOSTER_PROFESSION.send(Bukkit.getOnlinePlayers(), "multiplier", multFormatted, "profession", profession.getName());
        return CommandResult.SUCCESS;
    }
}
