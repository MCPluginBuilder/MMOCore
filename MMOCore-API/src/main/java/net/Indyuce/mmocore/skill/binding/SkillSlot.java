package net.Indyuce.mmocore.skill.binding;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.quest.trigger.SkillModifierTrigger;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class SkillSlot implements Unlockable {
    private final int slot;
    private final String formula;
    private final String name;
    private final List<String> lore;

    private final boolean unlockedByDefault;
    private final boolean canManuallyBind;

    @Nullable
    private final ClassSkill hardbind;

    private final List<SkillModifierTrigger> buffs = new ArrayList<>();

    @Deprecated
    public SkillSlot(int slot, int modelData, String formula, String name, List<String> lore, boolean unlockedByDefault, boolean canManuallyBind, List<SkillModifierTrigger> buffs) {
        this(slot, formula, name, lore, unlockedByDefault, canManuallyBind, buffs);
    }

    public SkillSlot(int slot, String formula, String name, List<String> lore, boolean unlockedByDefault, boolean canManuallyBind, List<SkillModifierTrigger> buffs) {
        this.slot = slot;
        this.formula = formula;
        this.name = name;
        this.lore = lore;
        this.canManuallyBind = canManuallyBind;
        this.unlockedByDefault = unlockedByDefault;
        this.buffs.addAll(buffs);
        this.hardbind = null;
    }

    public static final String SKILL_MODIFIER_TRIGGER_KEY = "mmocoreSkillSlot";

    @Deprecated(forRemoval = true)
    public SkillSlot(ConfigurationSection section) {
        this(null, section);
    }

    public SkillSlot(@Nullable PlayerClass clazz, ConfigurationSection section) {
        this.slot = Integer.parseInt(section.getName());
        this.formula = section.contains("formula") ? section.getString("formula") : "true";
        this.name = section.getString("name");
        this.lore = section.getStringList("lore");
        this.unlockedByDefault = section.getBoolean("unlocked-by-default", true);
        this.canManuallyBind = section.getBoolean("can-manually-bind", true);
        this.hardbind = clazz != null && section.contains("hardset")
                ? Objects.requireNonNull(UtilityMethods.prettyValueOf(clazz::getSkill, section.getString("hardset"), "Could not find skill with ID '%s'"))
                : null;

        // Load skill buffs
        if (section.contains("skill-buffs")) for (String skillBuff : section.getStringList("skill-buffs"))
            try {
                Validate.isTrue(skillBuff.startsWith("skill_buff"), "Must be a skill_buff trigger");
                final Trigger trigger = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(skillBuff));
                Validate.isTrue(trigger instanceof SkillModifierTrigger, "Not a skill_buff trigger");
                final SkillModifierTrigger mod = (SkillModifierTrigger) trigger;
                mod.updateKey(SKILL_MODIFIER_TRIGGER_KEY); // Fixes MMOCore issue #967
                buffs.add(mod);
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load skill buff '" + skillBuff + "' from skill slot '" + name + "': " + exception.getMessage());
            }
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    /**
     * @return Skill hardcoded into that skill slot, if any
     */
    @Nullable
    public ClassSkill getHardBind() {
        return hardbind;
    }

    @Deprecated
    public List<SkillModifierTrigger> getSkillModifierTriggers() {
        return getBuffs();
    }

    public List<SkillModifierTrigger> getBuffs() {
        return buffs;
    }

    public boolean canManuallyBind() {
        return canManuallyBind;
    }

    public boolean acceptsSkill(ClassSkill classSkill) {
        return classSkill.getSkill().matchesFormula(formula);
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "slot:" + slot;
    }

    /**
     * If we lock a slot that had a skill bound
     * to it we first unbind the attached skill.
     */
    @Override
    public void whenLocked(PlayerData playerData) {
        if (playerData.hasSkillBound(slot))
            playerData.unbindSkill(slot);
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {

    }
}
