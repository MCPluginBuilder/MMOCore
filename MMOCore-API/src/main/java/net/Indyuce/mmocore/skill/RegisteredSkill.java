package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.gui.util.IconOptions;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.util.formula.ScalingFormula;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

@Deprecated
public class RegisteredSkill {
    private final SkillHandler<?> handler;

    @Deprecated
    public RegisteredSkill(SkillHandler<?> handler) {
        this.handler = handler;
    }

    @Deprecated
    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;
    }

    @Deprecated
    public RegisteredSkill(SkillHandler<?> handler, String name, IconOptions icon, List<String> lore, @Nullable TriggerType triggerType) {
        this.handler = handler;
    }

    @Deprecated
    public SkillHandler<?> getHandler() {
        return handler;
    }

    @Deprecated
    public String getName() {
        return handler.getName();
    }

    @Deprecated
    public List<String> getLore() {
        return handler.getUiLore();
    }

    @Deprecated
    public List<String> getCategories() {
        return handler.getCategories();
    }

    @Deprecated
    public IconOptions getRawIcon() {
        return handler.getIcon();
    }

    @Deprecated
    public boolean hasParameter(@NotNull String parameter) {
        return handler.getModifiers().contains(parameter);
    }

    @Deprecated
    public TriggerType getTrigger() {
        return Objects.requireNonNull(handler.getDefaultTriggerType(), "Skill has no trigger");
    }

    @Deprecated
    public void addParameter(@NotNull String parameter, @NotNull ScalingFormula formula) {
        // Nope
    }

    @Deprecated
    public DecimalFormat getDecimalFormat(String parameter) {
        return handler.getParameterDecimalFormat(parameter);
    }

    @Deprecated
    public ScalingFormula getParameterInfo(String parameter) {
        throw new RuntimeException("Deprecated");
    }

    @Deprecated
    public boolean matchesFormula(String formula) {
        return MMOCoreUtils.evaluateSkillFormula(handler, formula);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredSkill that = (RegisteredSkill) o;
        return handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler);
    }

    //region Deprecated

    @Deprecated
    public RegisteredSkill(SkillHandler<?> handler, String name, ItemStack icon, List<String> lore, @Nullable TriggerType triggerType) {
        this(handler, name, IconOptions.from(icon), lore, triggerType);
    }

    @Deprecated
    public ItemStack getIcon() {
        return handler.getIcon().toItemStack();
    }

    /**
     * Skills modifiers are now called parameters.
     *
     * @see #hasParameter(String)
     */
    @Deprecated
    public boolean hasModifier(String modifier) {
        return hasParameter(modifier);
    }

    /**
     * Skill modifiers are now called parameters.
     *
     * @see #addParameter(String, ScalingFormula)
     */
    @Deprecated
    public void addModifier(String modifier, LinearValue linear) {
        // Nope
    }

    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public ScalingFormula getModifierInfo(String modifier) {
        return null;
    }

    @Deprecated
    public void addModifierIfNone(String mod, LinearValue defaultValue) {
        // Nope
    }

    //endregion
}
