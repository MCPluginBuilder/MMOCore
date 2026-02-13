package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommandTreeNode extends CommandTreeNode {
    public ReloadCommandTreeNode(CommandTreeNode parent) {
        super(parent, "reload");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        explorer.verbose("&eReloading " + MMOCore.plugin.getName() + " " + MMOCore.plugin.getDescription().getVersion() + "...");
        long ms = System.currentTimeMillis();

        MMOCore.plugin.initializePlugin(true);

        ms = System.currentTimeMillis() - ms;
        explorer.verbose("&e" + MMOCore.plugin.getName() + " " + MMOCore.plugin.getDescription().getVersion() + " successfully reloaded.");
        explorer.verbose("&eTime Taken: &6" + ms + "&ems");
        return CommandResult.SUCCESS;
    }
}
