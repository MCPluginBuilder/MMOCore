package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoCooldownCommandTreeNode extends CommandTreeNode {
	public NoCooldownCommandTreeNode(CommandTreeNode parent) {
		super(parent, "nocd");

		addArgument(Argument.PLAYER);
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		PlayerData data = PlayerData.get(player);
		data.noCooldown = !data.noCooldown;
		return explorer.success(ChatColor.YELLOW + "NoCD " + (data.noCooldown ? "enabled" : "disabled") + " for " + player.getName());
	}
}
