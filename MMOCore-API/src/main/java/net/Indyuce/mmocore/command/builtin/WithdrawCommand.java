package net.Indyuce.mmocore.command.builtin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.eco.Withdraw;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WithdrawCommand extends CommandTreeRoot {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argAmount;

    public WithdrawCommand(@NotNull ConfigurationSection config) {
        super(config);

        argPlayer = addArgument(Arguments.PLAYER_IF_OP);
        argAmount = addArgument(Argument.AMOUNT_INT.withFallback(explorer -> 0));
    }

    @Override
    @NotNull
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var amount = explorer.parse(argAmount);

        if (amount == 0) {
            new Withdraw(player).open();
            return CommandResult.SUCCESS;
        }

        int left = (int) MMOCore.plugin.economy.getEconomy().getBalance(player) - amount;
        if (left < 0) {
            Message.WITHDRAW_NOT_ENOUGH_MONEY.send(player, "left", -left);
            return CommandResult.FAILURE;
        }

        MMOCore.plugin.economy.getEconomy().withdrawPlayer(player, amount);
        new Withdraw(player).withdrawAlgorithm(amount);
        Message.WITHDRAW_SUCCESS.send(player, "worth", amount);
        return CommandResult.SUCCESS;
    }
}
