package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.util.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandTrigger extends Trigger {
	private final String command;

	public CommandTrigger(MMOLineConfig config) {
		super(config);

		config.validate("format");
		command = config.getString("format");
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		String formattedCommand = format(player.getPlayer());
		if (SchedulerAdapter.isFolia()) {
			SchedulerAdapter.runAtEntity(MMOCore.plugin, player.getPlayer(), () -> {
				player.getPlayer().performCommand(formattedCommand);
			});
		} else {
			SchedulerAdapter.runTask(MMOCore.plugin, () -> {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
			});
		}
	}

	@BackwardsCompatibility(version = "1.12-SNAPSHOT")
	private String format(Player player) {
		return MMOCore.plugin.placeholderParser.parse(player, command.replace("%player%", player.getName()).replace("%player_name%", player.getName()));
	}
}
