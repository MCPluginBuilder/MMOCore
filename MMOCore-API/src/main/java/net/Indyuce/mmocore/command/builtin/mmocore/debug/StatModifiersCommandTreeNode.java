package net.Indyuce.mmocore.command.builtin.mmocore.debug;

import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class StatModifiersCommandTreeNode extends CommandTreeNode {
    private final Argument<String> argStat;

    public StatModifiersCommandTreeNode(CommandTreeNode parent) {
        super(parent, "statmods");

        argStat = addArgument(Argument.STAT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return explorer.fail("This command can only be used by a player.");
        PlayerData data = PlayerData.get((Player) sender);

        final var stat = explorer.parse(argStat);
        StatInstance instance = data.getMMOPlayerData().getStatMap().getInstance(stat);
        explorer.verbose("Stat Modifiers (" + instance.getKeys().size() + "):");
        for (String key : instance.getKeys()) {
            StatModifier mod = instance.getModifier(key);
            explorer.verbose("-> '" + key + "' " + mod.getValue() + " " + mod.getType().name() + " " + mod.getSlot() + " " + mod.getSource());
        }

        return CommandResult.SUCCESS;
    }
}
