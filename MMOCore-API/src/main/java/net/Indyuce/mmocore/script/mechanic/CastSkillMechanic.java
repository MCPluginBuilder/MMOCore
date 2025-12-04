package net.Indyuce.mmocore.script.mechanic;

import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.event.skill.SkillCastEvent;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CastSkillMechanic extends TargetMechanic {
    private final String mmocoreSkillId;
    private final boolean applyRequirements;

    public CastSkillMechanic(ConfigObject config) {
        super(config);

        this.mmocoreSkillId = config.string("skill", "s", "name", "n");
        this.applyRequirements = config.bool(true, "requirements", "req", "r");
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        final var targetData = PlayerData.get(target.getUniqueId());

        final var classSkill = targetData.getProfess().getSkill(mmocoreSkillId);
        Validate.notNull(classSkill, "Skill " + mmocoreSkillId + " not found for player " + target.getName() + " with class " + targetData.getProfess().getId());
        final var castable = classSkill.toCastable(targetData);

        // Cast without checking mana/cd requirements
        if (!applyRequirements) {
            // TODO [skill update] improve code, maybe move it to Skill interface in MythicLib
            SkillHandler handler = castable.getHandler();
            SkillMetadata skillMetadata = SkillMetadata.of(targetData.getMMOPlayerData()).clone(castable);
            SkillResult result = handler.getResult(skillMetadata);
            if (!result.isSuccessful()) return;

            // Call first Bukkit event
            final PlayerCastSkillEvent called = new PlayerCastSkillEvent(castable, skillMetadata, result);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled()) return;

            ((SkillHandler) castable.getHandler()).whenCast(result, skillMetadata);
            Bukkit.getPluginManager().callEvent(new SkillCastEvent(castable, skillMetadata, result));
        }

        // Cast and check mana/cooldown requirements
        else {
            castable.cast(targetData.getMMOPlayerData());
        }
    }
}
