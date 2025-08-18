package net.Indyuce.mmocore.loot.chest.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;

import java.util.Arrays;
import java.util.List;

public class WorldCondition extends Condition {
	private final List<String> names;

	public WorldCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split(","));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		return names.contains(entity.getLocation().getWorld().getName()) || names.contains("__global__");
	}
}
