package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.SmartGive;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CoinsCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argWorth;

    public CoinsCommandTreeNode(CommandTreeNode parent) {
        super(parent, "coins");

        argPlayer = addArgument(Argument.PLAYER);
        argWorth = addArgument(Argument.AMOUNT_INT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var amount = explorer.parse(argWorth);

        ItemStack coins = new CurrencyItemBuilder("GOLD_COIN", 1).build();
        coins.setAmount(amount);
        new SmartGive(player).give(coins);
        return CommandResult.SUCCESS;
    }
}
