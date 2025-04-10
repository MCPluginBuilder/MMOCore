package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.eco.Withdraw;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WithdrawCommand extends RegisteredCommand {
    public WithdrawCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.WITHDRAW);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("mmocore.currency"))
            return false;

        final Player player;
        if (args.length >= 2 && sender.hasPermission("mmocore.admin")) player = Bukkit.getPlayer(args[0]);
        else if (args.length == 1 && sender.hasPermission("mmocore.admin")) {
            Player tryFirstArg = Bukkit.getPlayer(args[0]);
            player = tryFirstArg != null ? tryFirstArg : sender instanceof Player ? (Player) sender : null;
        } else if (sender instanceof Player) player = (Player) sender;
        else player = null;
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
            return true;
        }

        int amount;
        try {
            if (args.length == 0) amount = 0;
            else amount = Integer.parseInt(args[args.length - 1]);
            Validate.isTrue(amount >= 0);
        } catch (IllegalArgumentException exception) {
            ConfigMessage.fromKey("wrong-number", "arg", args[0]).send(sender);
            return true;
        }

        Withdraw request = new Withdraw(player);

        if (amount == 0) {
            request.open();
            return true;
        }

        int left = (int) MMOCore.plugin.economy.getEconomy().getBalance(player) - amount;
        if (left < 0) {
            ConfigMessage.fromKey("not-enough-money", "left", "" + -left).send(player);
            return true;
        }

        MMOCore.plugin.economy.getEconomy().withdrawPlayer(player, amount);
        request.withdrawAlgorithm(amount);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        ConfigMessage.fromKey("withdrew", "worth", amount).send(player);
        return true;
    }
}
