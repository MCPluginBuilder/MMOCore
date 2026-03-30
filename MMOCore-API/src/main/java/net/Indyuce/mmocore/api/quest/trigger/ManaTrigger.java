package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomDecimalAmount;

public class ManaTrigger extends Trigger {
	private final RandomDecimalAmount amount;
	private final Operation operation;

	public ManaTrigger(MMOLineConfig config) {
		super(config);

		amount = new RandomDecimalAmount(config.getString("amount"));
		operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
	}

	@Override
	public void apply(PlayerData player) {

		// Give mana
		if (operation == Operation.GIVE)
			player.giveMana(amount.roll(), ResourceUpdateReason.MECHANIC);

			// Set mana
		else if (operation == Operation.SET)
			player.setMana(amount.roll(), ResourceUpdateReason.MECHANIC);

			// Take mana
		else
			player.giveMana(-amount.roll(), ResourceUpdateReason.MECHANIC);
	}

	public enum Operation {
		GIVE,
		SET,
		TAKE
	}
}
