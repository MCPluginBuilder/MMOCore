package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.SmartGive;
import net.Indyuce.mmocore.util.item.CurrencyItemBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NoteCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argWorth;

    public NoteCommandTreeNode(CommandTreeNode parent) {
        super(parent, "note");

        argPlayer = addArgument(Argument.PLAYER);
        argWorth = addArgument(Argument.AMOUNT_INT.withKey("worth"));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var worth = explorer.parse(argWorth);

        new SmartGive(player).give(new CurrencyItemBuilder("NOTE", worth).build());
        return CommandResult.SUCCESS;
    }
}
