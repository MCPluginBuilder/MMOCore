package net.Indyuce.mmocore.api.quest.objective;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class GoToObjective extends Objective {
	private final Location loc;
	private final double range;

	public GoToObjective(ConfigurationSection section, MMOLineConfig config) {
		super(section);

		config.validate("range", "world", "x", "y", "z");

		range = config.getDouble("range");
		World world = Bukkit.getWorld(config.getString("world"));
		Validate.notNull(world, "Could not find world " + config.getString("world"));
		loc = new Location(world, config.getInt("x"), config.getInt("y"), config.getInt("z"));
	}

	@Override
	public ObjectiveProgress newProgress(QuestProgress questProgress) {
		return new GotoProgress(questProgress, this);
	}

	public class GotoProgress extends ObjectiveProgress implements Listener {
		public GotoProgress(QuestProgress questProgress, Objective objective) {
			super(questProgress, objective);
		}

		@EventHandler
		public void a(PlayerMoveEvent event) {
			if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
				return;

			Player player = event.getPlayer();
			if (getPlayer().isOnline() && player.equals(getPlayer().getPlayer())) {
				if (player.getWorld().equals(loc.getWorld()) && ((player.getLocation().distance(loc) - 0.5d) < range))
					getQuestProgress().completeObjective();
			}
		}

		@Override
		public String formatLore(String lore) {
			return lore;
		}
	}
}
