package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Booster;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ListCommandTreeNode extends CommandTreeNode {
    public ListCommandTreeNode(CommandTreeNode parent) {
        super(parent, "list");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return CommandResult.FAILURE;

        explorer.verbose("&e----------------------------------------------------");
        for (Booster booster : MMOCore.plugin.boosterManager.getActive())
            if (!booster.isTimedOut())
                MythicLib.plugin.getVersion().getWrapper().sendJson((Player) sender, "{\"text\":\"" + "- "
                        + MythicLib.plugin.getMMOConfig().decimal.format((1 + booster.getExtra())) + "x" + " Booster - "
                        + (!booster.hasProfession() ? "ExploreAttributesCommand" : booster.getProfession().getName()) + " - "
                        + new DelayFormat().format(booster.getCreationDate() + booster.getDuration() - System.currentTimeMillis())
                        + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/mmocore booster remove " + booster.getUniqueId().toString()
                        + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to remove.\"}}}");
        explorer.verbose("&e----------------------------------------------------");

        return CommandResult.SUCCESS;
    }
}
