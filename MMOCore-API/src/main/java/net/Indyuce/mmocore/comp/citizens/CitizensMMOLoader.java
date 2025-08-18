package net.Indyuce.mmocore.comp.citizens;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import org.bukkit.configuration.ConfigurationSection;

public class CitizensMMOLoader extends MMOLoader {

	@Override
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {

		if (config.getKey().equals("talkto"))
			return new TalktoCitizenObjective(section, config);

		if (config.getKey().equals("getitem"))
			return new GetItemObjective(section, config);

		return null;
	}
}
