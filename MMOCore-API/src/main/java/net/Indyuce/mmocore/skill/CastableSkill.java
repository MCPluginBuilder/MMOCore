package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Message;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;

public class CastableSkill extends Skill {
    private final ClassSkill skill;

    /**
     * This fixes a bug where the CastableSkill instance is created
     * before a skill level change happens (due to player level up)
     */
    private final Provider<Integer> skillLevel;

    private final PlayerData caster;

    @Deprecated
    public CastableSkill(ClassSkill skill, int fixedLevel) {
        this(skill, fixedLevel, null);
    }

    public CastableSkill(@NotNull ClassSkill skill, int fixedLevel, @NotNull PlayerData caster) {
        super(skill.getSkill());

        this.skill = skill;
        this.skillLevel = () -> fixedLevel;
        this.caster = caster;
    }

    public CastableSkill(@NotNull ClassSkill skill, @NotNull PlayerData caster) {
        super(skill.getSkill());

        this.skill = skill;
        this.caster = caster;
        this.skillLevel = () -> this.caster.getSkillLevel(skill.getSkill());
    }

    // TODO non conventional use of trigger
    @Override
    public TriggerType getTrigger() {
        return skill.getTrigger();
    }

    public ClassSkill getSkill() {
        return skill;
    }

    @Override
    public boolean getResult(SkillMetadata skillMeta) {
        PlayerData playerData = PlayerData.get(skillMeta.getCaster().getData());
        boolean notify = !getTrigger().isSilent();

        // Skill is not usable yet
        if (!playerData.hasUnlockedLevel(skill)) {
            if (notify) Message.SKILL_LEVEL_NOT_MET.send(playerData);
            return false;
        }

        // Global cooldown check
        if (!getTrigger().isPassive() && playerData.getActivityTimeOut(PlayerActivity.CAST_SKILL) > 0)
            return false;

        // Cooldown check
        if (skillMeta.getCaster().getData().getCooldownMap().isOnCooldown(this)) {
            if (notify) {
                var cooldown = skillMeta.getCaster().getData().getCooldownMap().getCooldown(this);
                var cdFormatted = MythicLib.plugin.getMMOConfig().decimal.format(cooldown);
                Message.CASTING_ON_COOLDOWN.send(playerData, "skill", skill.getSkill().getName(), "cooldown", cdFormatted);
            }
            return false;
        }

        // Mana cost
        var manaCost = skillMeta.getParameter("mana");
        if (playerData.getMana() < manaCost) {
            if (notify) {
                final var manaRequired = manaCost - playerData.getMana();
                final var manaReqFormatted = MythicLib.plugin.getMMOConfig().decimal.format(manaRequired);
                final var manaCostFormatted = MythicLib.plugin.getMMOConfig().decimal.format(manaCost);
                Message.CASTING_NO_MANA.send(playerData,
                        "skill", skill.getSkill().getName(),
                        "mana-required", manaReqFormatted,
                        "mana-cost", manaCostFormatted,
                        "mana", playerData.getProfess().getManaDisplay().getName());
            }
            return false;
        }

        // Stamina cost
        var staminaCost = skillMeta.getParameter("stamina");
        if (playerData.getStamina() < staminaCost) {
            if (notify) {
                final var staminaRequired = manaCost - playerData.getStamina();
                final var staminaReqFormatted = MythicLib.plugin.getMMOConfig().decimal.format(staminaRequired);
                final var staminaCostFormatted = MythicLib.plugin.getMMOConfig().decimal.format(staminaCost);
                Message.CASTING_NO_STAMINA.send(playerData,
                        "skill", skill.getSkill().getName(),
                        "stamina-required", staminaReqFormatted,
                        "stamina-cost", staminaCostFormatted);
            }
            return false;
        }

        // Ability flag
        if (!MythicLib.plugin.getFlags().isFlagAllowed(skillMeta.getCaster().getPlayer(), CustomFlag.MMO_ABILITIES))
            return false;

        return true;
    }

    @Override
    public void whenCast(SkillMetadata skillMeta) {
        PlayerData casterData = PlayerData.get(skillMeta.getCaster().getData());

        // Apply cooldown, mana and stamina costs
        if (!casterData.noCooldown) {

            // Cooldown
            double flatCooldownReduction = Math.max(0, Math.min(1, skillMeta.getCaster().getStat("COOLDOWN_REDUCTION") / 100));
            CooldownInfo cooldownHandler = skillMeta.getCaster().getData().getCooldownMap().applyCooldown(this, skillMeta.getParameter("cooldown"));
            cooldownHandler.reduceInitialCooldown(flatCooldownReduction);

            casterData.giveMana(-skillMeta.getParameter("mana"), ResourceUpdateReason.SKILL);
            casterData.giveStamina(-skillMeta.getParameter("stamina"), ResourceUpdateReason.SKILL);
        }

        if (!getTrigger().isPassive()) casterData.setLastActivity(PlayerActivity.CAST_SKILL);
    }

    @Override
    public double getParameter(String mod) {
        return skill.getParameter(mod, this.skillLevel.get(), this.caster);
    }
}
