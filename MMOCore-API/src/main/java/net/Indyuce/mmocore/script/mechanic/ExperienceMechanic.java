package net.Indyuce.mmocore.script.mechanic;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ExperienceMechanic extends TargetMechanic {
    private final NumericExpression amount;
    private final EXPSource source;
    @Nullable
    private final Profession profession;

    public ExperienceMechanic(ConfigObject config) {
        super(config);

        amount = config.numericExpr("amount");

        if (config.contains("profession")) {
            String id = config.getString("profession").toLowerCase().replace("_", "-");
            Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
            profession = MMOCore.plugin.professionManager.get(id);
        } else profession = null;
        source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        PlayerData targetData = PlayerData.get(target.getUniqueId());

        if (profession != null) profession.giveExperience(targetData, amount.evaluate(meta), null, source);
        else targetData.getProfess().giveExperience(targetData, amount.evaluate(meta), null, source);
    }
}
