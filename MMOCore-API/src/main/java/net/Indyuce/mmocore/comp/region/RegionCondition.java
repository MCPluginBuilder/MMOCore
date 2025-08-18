package net.Indyuce.mmocore.comp.region;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.chest.condition.ConditionInstance;

import java.util.Arrays;
import java.util.List;

public class RegionCondition extends Condition {
	private final List<String> names;

	public RegionCondition(MMOLineConfig config) {
		super(config);

		config.validate("name");
		names = Arrays.asList(config.getString("name").split(","));
	}

	@Override
	public boolean isMet(ConditionInstance entity) {
		return entity.getRegionStream().anyMatch(names::contains);
	}
}
