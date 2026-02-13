package net.Indyuce.mmocore.command.builtin.mmocore.debug;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.stats.StatInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatValueCommandTreeNode extends CommandTreeNode {
    private final Argument<String> argStat;

    public StatValueCommandTreeNode(CommandTreeNode parent) {
        super(parent, "statvalue");

        argStat = addArgument(Argument.STAT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return explorer.fail("This command can only be used by a player.");
        PlayerData data = PlayerData.get((Player) sender);

        final var statId = explorer.parse(argStat);
        StatInfo stat = StatInfo.valueOf(statId);
        return explorer.success("Stat Value (&b" + stat.name + "&f): &a" + data.getStats().getStat(stat.name));
    }
}
