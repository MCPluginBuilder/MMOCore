package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.Profession;
import org.jetbrains.annotations.Nullable;

public class ExperienceTrigger extends Trigger {
	private final RandomAmount amount;
	private final EXPSource source;
	@Nullable
	private final Profession profession;

	public ExperienceTrigger(MMOLineConfig config) {
		super(config);

		config.validate("amount");

        if (config.contains("profession")) {
            String id = config.getString("profession").toLowerCase().replace("_", "-");
            Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
            profession = MMOCore.plugin.professionManager.get(id);
        } else profession = null;
		amount = new RandomAmount(config.getString("amount"));
		source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
	}

	@Override
	public void apply(PlayerData player) {
        if (profession != null) profession.giveExperience(player, amount.calculate(), null, source);
        else player.getProfess().giveExperience(player, amount.calculate(), null, source);
    }
}
