package net.Indyuce.mmocore.command.builtin.mmocore.debug;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.stats.StatInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatValueCommandTreeNode extends CommandTreeNode {
    public StatValueCommandTreeNode(CommandTreeNode parent) {
        super(parent, "statvalue");

        addArgument(Argument.STAT);
    }

    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return CommandResult.FAILURE;
        }
        PlayerData data = PlayerData.get((Player) sender);

        StatInfo stat = StatInfo.valueOf(UtilityMethods.enumName(args[2]));
        sender.sendMessage(DebugCommandTreeNode.commandPrefix + "Stat Value (" + ChatColor.BLUE + stat.name + ChatColor.WHITE + "): "
                + ChatColor.GREEN + data.getStats().getStat(stat.name));

        return CommandResult.SUCCESS;
    }
}
