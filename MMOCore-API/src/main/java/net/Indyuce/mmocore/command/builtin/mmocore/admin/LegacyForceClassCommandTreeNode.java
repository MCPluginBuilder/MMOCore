package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated
 * @see net.Indyuce.mmocore.command.builtin.mmocore.clazz.ForceCommandTreeNode
 */
@Deprecated
public class LegacyForceClassCommandTreeNode extends CommandTreeNode {
	public LegacyForceClassCommandTreeNode(CommandTreeNode parent) {
		super(parent, "force-class");

		addArgument(Argument.PLAYER);
		addArgument(Arguments.CLASS);
	}

	@Override
	public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		String format = args[3].toUpperCase().replace("-", "_");
		if (!MMOCore.plugin.classManager.has(format)) {
			sender.sendMessage(ChatColor.RED + "Could not find class " + format + ".");
			return CommandResult.FAILURE;
		}

		PlayerClass profess = MMOCore.plugin.classManager.get(format);

		PlayerData data = PlayerData.get(player);
		final var called = new PlayerChangeClassEvent(data, profess, PlayerChangeClassEvent.Reason.COMMAND_FORCE);
		Bukkit.getPluginManager().callEvent(called);
		if (called.isCancelled()) return explorer.fail("Bukkit event canceled");

		data.setClass(profess);
		return explorer.success(ChatColor.GOLD + player.getName()
				+ ChatColor.YELLOW + " is now a " + ChatColor.GOLD + profess.getName());
	}
}
