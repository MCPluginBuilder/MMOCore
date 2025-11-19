package net.Indyuce.mmocore.script.mechanic;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CastSkillMechanic extends TargetMechanic {
    private final String mmocoreSkillId;

    public CastSkillMechanic(ConfigObject config) {
        super(config);

        this.mmocoreSkillId = config.string("skill", "s", "name", "n");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        final var targetData = PlayerData.get(target.getUniqueId());

        final var classSkill = targetData.getProfess().getSkill(mmocoreSkillId);
        Validate.notNull(classSkill, "Skill " + mmocoreSkillId + " not found for player " + target.getName() + " with class " + targetData.getProfess().getId());

        classSkill.toCastable(targetData).cast(targetData.getMMOPlayerData());
    }
}
