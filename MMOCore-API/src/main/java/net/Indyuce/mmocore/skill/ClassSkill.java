package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.util.formula.FormulaFailsafeException;
import net.Indyuce.mmocore.util.formula.ScalingFormula;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassSkill implements CooldownObject, Unlockable {
    private final RegisteredSkill skill;
    private final int unlockLevel, maxSkillLevel;
    private final boolean unlockedByDefault, permanent, upgradable;
    private final Map<String, ScalingFormula> parameters = new HashMap<>();

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel) {
        this(skill, unlockLevel, maxSkillLevel, true);
    }

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault) {
        this(skill, unlockLevel, maxSkillLevel, unlockedByDefault, MMOCore.plugin.configManager.passiveSkillsNeedBinding);
    }

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault, boolean needsBinding) {
        this(skill, unlockLevel, maxSkillLevel, unlockedByDefault, needsBinding, true);
    }

    /**
     * Class used to save information about skills IN A CLASS CONTEXT
     * i.e at which level the skill can be unlocked, etc.
     * <p>
     * This constructor can be used by other plugins to register class
     * skills directly without the use of class config files.
     * <p>
     * It is also used by the MMOCore API to force players to cast abilities.
     */
    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault, boolean needsBinding, boolean upgradable) {
        this.skill = skill;
        this.unlockLevel = unlockLevel;
        this.maxSkillLevel = maxSkillLevel;
        this.unlockedByDefault = unlockedByDefault;
        this.permanent = !needsBinding && skill.getTrigger().isPassive();
        this.upgradable = upgradable;

        for (String param : skill.getHandler().getParameters())
            this.parameters.put(param, skill.getParameterInfo(param));
    }

    public ClassSkill(RegisteredSkill skill, ConfigurationSection config) {
        this.skill = skill;
        unlockLevel = config.getInt("level");
        maxSkillLevel = config.getInt("max-level");
        unlockedByDefault = config.getBoolean("unlocked-by-default", true);
        permanent = !config.getBoolean("needs-bound", MMOCore.plugin.configManager.passiveSkillsNeedBinding) && skill.getTrigger().isPassive();
        upgradable = config.getBoolean("upgradable", true);

        for (String param : skill.getHandler().getParameters()) {
            var fallback = skill.getParameterInfo(param);
            var formulaConfig = config.get(param);
            var formula = formulaConfig == null ? fallback : ScalingFormula.fromConfig(formulaConfig, fallback);
            this.parameters.put(param, formula);
        }
    }

    @NotNull
    public RegisteredSkill getSkill() {
        return skill;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public boolean hasMaxLevel() {
        return maxSkillLevel > 0;
    }

    public int getMaxLevel() {
        return maxSkillLevel;
    }

    public boolean isUpgradable() {
        return upgradable;
    }

    @Override
    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    /**
     * @return Permanent skills are passive skills which do
     * not have to be bound in order to apply their effects.
     * Permanent skills can only be passive skills.
     */
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "skill:" + skill.getHandler().getId().toLowerCase();
    }

    @Override
    public void whenLocked(PlayerData playerData) {

        // Unbind the skill if necessary
        new HashMap<>(playerData.getBoundSkills()).forEach((slot, bound) -> {
            if (this.equals(bound.getClassSkill()))
                playerData.unbindSkill(slot);
        });

        // Update stats to flush permanent skill
        if (isPermanent()) playerData.getStats().updateStats();
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {

        // Update stats to register permanent skill
        if (isPermanent()) playerData.getStats().updateStats();
    }

    /**
     * This method can only override default parameters and
     * will throw an error when trying to define non-existing modifiers
     */
    public void addParameter(@NotNull String parameter, @NotNull ScalingFormula formula) {
        Validate.isTrue(parameters.containsKey(parameter), "Could not find parameter called '" + parameter + "'");
        parameters.put(parameter, formula);
    }

    @NotNull
    public ScalingFormula getParameterFormula(@NotNull String parameter) {
        return Objects.requireNonNull(parameters.get(parameter), "Could not find parameter called '" + parameter + "'");
    }

    public double getParameter(@NotNull String parameter, int level, @Nullable PlayerData caster) {
        try {
            return getParameterFormula(parameter).evaluate(level, caster);
        } catch (FormulaFailsafeException exception) {
            exception.log("Could not evaluate parameter '%s' of skill '%s'", parameter, getSkill().getHandler().getId());
            return exception.getFailsafe();
        }
    }

    public double getParameter(@NotNull String parameter, @NotNull PlayerData caster) {
        return getParameter(parameter, caster.getSkillLevel(this.skill), caster);
    }

    @NotNull
    public List<String> calculateLore(PlayerData data) {
        return calculateLore(data, data.getSkillLevel(skill));
    }

    @NotNull
    public List<String> calculateLore(PlayerData data, int skillLevel) {

        // Calculate placeholders
        var placeholders = new Placeholders();

        // Skill parameters
        for (var param : parameters.keySet()) {
            var baseValue = getParameter(param, skillLevel, data);
            var modifiedValue = data.getMMOPlayerData().getSkillModifierMap().calculateValue(skill.getHandler(), baseValue, param);
            var formatted = skill.getDecimalFormat(param).format(modifiedValue);

            placeholders.register(param, formatted);
        }

        placeholders.register("mana_name", data.getProfess().getManaDisplay().getName());
        placeholders.register("mana_color", data.getProfess().getManaDisplay().getFull().toString());

        // Build string arraylist
        List<String> list = new ArrayList<>();
        skill.getLore().forEach(str -> list.add(placeholders.apply(data.getPlayer(), str)));

        return list;
    }

    @NotNull
    public CastableSkill toCastable(PlayerData caster) {
        return new CastableSkill(this, caster);
    }

    /**
     * Be careful, this method creates a new UUID each time it
     * is called. It needs to be saved somewhere when trying to
     * unregister the passive skill from the skill map later on.
     */
    @NotNull
    public PassiveSkill toPassive(PlayerData caster) {
        Validate.isTrue(skill.getTrigger().isPassive(), "Skill is active");
        return new PassiveSkill("MMOCore" + (permanent ? "Permanent" : "Passive") + "Skill", skill.getTrigger(), toCastable(caster), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    @Override
    public String getCooldownPath() {
        return "skill_" + skill.getHandler().getId();
    }

    //region Deprecated

    /**
     * @see #getParameterFormula(String)
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public double getModifier(String modifier, int level) {
        return getParameter(modifier, level, null);
    }

    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public void addModifier(String modifier, LinearValue linear) {
        addParameter(modifier, linear.adapt());
    }

    @Deprecated
    public boolean needsBound() {
        return getSkill().getTrigger().isPassive() && !isPermanent();
    }

    /**
     * This method can only override default parameters and
     * will throw an error when trying to define non-existing modifiers
     *
     * @see #addParameter(String, ScalingFormula)
     * @deprecated
     */
    @Deprecated
    public void addParameter(String parameter, LinearValue linear) {
        addParameter(parameter, linear.adapt());
    }

    @Deprecated
    public double getParameter(String parameter, int level) {
        return getParameter(parameter, level, null);
    }

    //endregion
}