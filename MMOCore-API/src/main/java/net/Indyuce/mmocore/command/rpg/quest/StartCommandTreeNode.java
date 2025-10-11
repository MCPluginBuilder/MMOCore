package net.Indyuce.mmocore.command.rpg.quest;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommandTreeNode extends CommandTreeNode {
	public StartCommandTreeNode(CommandTreeNode parent) {
		super(parent, "start");

		addArgument(Argument.PLAYER);
		addArgument(Arguments.QUEST);
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		Player player = Bukkit.getPlayer(args[2]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Could not find player called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		Quest quest;
		try {
			quest = MMOCore.plugin.questManager.get(args[3].replace("_", "-").toLowerCase());
		} catch (Exception exception) {
			sender.sendMessage(ChatColor.RED + "Could not find quest with ID " + args[3].replace("_", "-").toLowerCase() + ".");
			return CommandResult.FAILURE;
		}

		PlayerQuests quests = PlayerData.get(player).getQuestData();
		if (quests.hasCurrent()) {
			if (sender instanceof Player)
				sender.sendMessage(ChatColor.RED + player.getName() + " already has an ongoing quest.");
			return CommandResult.SUCCESS;
		}

		quests.start(quest);
		if (sender instanceof Player)
			sender.sendMessage(ChatColor.YELLOW + player.getName() + " successfully started " + quest.getName() + ".");
		return CommandResult.SUCCESS;
	}
}
