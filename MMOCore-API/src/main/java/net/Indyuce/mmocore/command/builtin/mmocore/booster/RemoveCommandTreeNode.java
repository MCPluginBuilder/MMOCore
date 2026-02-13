package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.Booster;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RemoveCommandTreeNode extends CommandTreeNode {
    private final Argument<Booster> argBooster;

    public RemoveCommandTreeNode(CommandTreeNode parent) {
        super(parent, "remove");

        argBooster = addArgument(Arguments.BOOSTER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var booster = explorer.parse(argBooster);
        MMOCore.plugin.boosterManager.unregister(booster);
        return explorer.success("Successfully unregistered this booster.");
    }
}
