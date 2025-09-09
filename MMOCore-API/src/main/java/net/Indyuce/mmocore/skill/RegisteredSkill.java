package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.util.IconOptions;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.formula.BooleanExpression;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.util.formula.LinearScalingFormula;
import net.Indyuce.mmocore.util.formula.ScalingFormula;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;

public class RegisteredSkill {
    private final SkillHandler<?> handler;
    private final String name;
    private final Map<String, ScalingFormula> defaultParameters = new HashMap<>();

    private final Map<String, DecimalFormat> parameterDecimalFormats = new HashMap<>();

    private final IconOptions icon;
    private final List<String> lore;
    private final List<String> categories;
    private final TriggerType triggerType;

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        name = Objects.requireNonNull(config.getString("name"), "Could not find skill name");
        icon = IconOptions.from(config.get("material"));
        lore = Objects.requireNonNull(config.getStringList("lore"), "Could not find skill lore");

        // Trigger type
        triggerType = getHandler().isTriggerable() ? (config.contains("passive-type") ? TriggerType.valueOf(UtilityMethods.enumName(config.getString("passive-type"))) : TriggerType.CAST) : TriggerType.API;

        // Basic Categories
        categories = config.getStringList("categories");
        categories.add(getHandler().getId());
        categories.add(triggerType.isPassive() ? "PASSIVE" : "ACTIVE");

        // Load default modifier formulas and decimal formats.
        for (String param : handler.getParameters()) {
            @Nullable var object = config.get("parameters." + param);
            if (object == null) object = config.get(param); // [Backwards compatibility] Old syntax

            defaultParameters.put(param, ScalingFormula.fromConfig(object, null));

            // Decimal format
            if (object instanceof ConfigurationSection && ((ConfigurationSection) object).contains("decimal-format"))
                parameterDecimalFormats.put(param, new DecimalFormat(((ConfigurationSection) object).getString("decimal-format")));
        }

        /*
         * This is so that SkillAPI skill level matches the MMOCore skill level
         * https://gitlab.com/phoenix-dvpmt/mmocore/-/issues/531
         */
        defaultParameters.put("level", new LinearScalingFormula(0, 1));
    }

    public RegisteredSkill(SkillHandler<?> handler, String name, IconOptions icon, List<String> lore, @Nullable TriggerType triggerType) {
        this.handler = handler;
        this.name = name;
        this.icon = IconOptions.from(icon);
        this.lore = lore;
        this.triggerType = triggerType;
        this.categories = new ArrayList<>();
    }

    public SkillHandler<?> getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getCategories() {
        return categories;
    }

    @NotNull
    public IconOptions getRawIcon() {
        return icon;
    }

    public boolean hasParameter(@NotNull String parameter) {
        return defaultParameters.containsKey(parameter);
    }

    @NotNull
    public TriggerType getTrigger() {
        return Objects.requireNonNull(triggerType, "Skill has no trigger");
    }

    public void addParameter(@NotNull String parameter, @NotNull ScalingFormula formula) {
        defaultParameters.put(parameter, formula);
    }

    @NotNull
    public DecimalFormat getDecimalFormat(String parameter) {
        return parameterDecimalFormats.getOrDefault(parameter, MythicLib.plugin.getMMOConfig().decimal);
    }

    /**
     * @return Modifier formula.
     *         Not null as long as the modifier is well-defined
     */
    @NotNull
    public ScalingFormula getParameterInfo(String parameter) {
        return Objects.requireNonNull(defaultParameters.get(parameter), String.format("Could not find parameter %s", parameter));
    }

    public boolean matchesFormula(String formula) {
        String parsedExpression = formula;
        for (String category : categories)
            parsedExpression = parsedExpression.replace("<" + category + ">", "true");
        parsedExpression = parsedExpression.replaceAll("<.*?>", "false");
        return BooleanExpression.eval(parsedExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredSkill that = (RegisteredSkill) o;
        return handler.equals(that.handler) && triggerType.equals(that.triggerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, triggerType);
    }

    //region Deprecated

    @Deprecated
    public RegisteredSkill(SkillHandler<?> handler, String name, ItemStack icon, List<String> lore, @Nullable TriggerType triggerType) {
        this(handler, name, IconOptions.from(icon), lore, triggerType);
    }

    @Deprecated
    public ItemStack getIcon() {
        return icon.toItemStack();
    }

    /**
     * Skills modifiers are now called parameters.
     *
     * @see #hasParameter(String)
     */
    @Deprecated
    public boolean hasModifier(String modifier) {
        return defaultParameters.containsKey(modifier);
    }

    /**
     * Skill modifiers are now called parameters.
     *
     * @see #addParameter(String, ScalingFormula)
     */
    @Deprecated
    public void addModifier(String modifier, LinearValue linear) {
        defaultParameters.put(modifier, linear.adapt());
    }

    /**
     * Skill modifiers are now called parameters.
     *
     * @see #getParameterInfo(String)
     */
    @Deprecated
    public ScalingFormula getModifierInfo(String modifier) {
        return defaultParameters.get(modifier);
    }

    @Deprecated
    public void addModifierIfNone(String mod, LinearValue defaultValue) {
        if (!hasParameter(mod)) addModifier(mod, defaultValue);
    }

    //endregion
}
