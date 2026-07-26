package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Lazy;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AttributeInstance {

    private final MMOPlayerData owner;

    private final String attributeId, mythicLibstatId;

    /**
     * Using a lazy value allows to flush values. When MMOCore
     * is reloaded, references to dead instances of player attributes
     * remain in attribute instances and need to be flushed.
     */
    private final Lazy<PlayerAttribute> attribute;

    /**
     * Unique ID used to store the modifier that corresponds to the
     * base value of the stat/attribute.
     */
    private static final UUID BASE_MODIFIER_UNIQUE_ID = UUID.randomUUID();

    public static final String MODIFIER_KEY = "mmocore_attribute";

    public AttributeInstance(@NotNull PlayerData owner, @NotNull String attributeId) {
        this.owner = owner.getMMOPlayerData();
        this.attributeId = attributeId;
        this.mythicLibstatId = asMythicLibStat(this.attributeId);
        this.attribute = Lazy.persistent(() -> MMOCore.plugin.attributeManager.get(this.attributeId));
    }

    public void flushReference() {
        this.attribute.flush();
    }

    /**
     * @return MythicLib handle
     * @implNote Cannot use a direct reference as
     *         it changes with time due to profiles
     */
    @NotNull
    private StatInstance getHandle() {
        return this.owner.getStatMap().getInstance(this.mythicLibstatId);
    }

    /**
     * Used to store the player's attribute values even when
     * attributes are missing from the console. This is error-proof
     * and avoids some data corruption.
     *
     * @return ID of attribute
     */
    @NotNull
    public String getAttributeId() {
        return attributeId;
    }

    /**
     * @return Null if attribute does not exist anymore.
     */
    @Nullable
    public PlayerAttribute getAttribute() {
        return attribute.get();
    }

    @NotNull
    public static String asMythicLibStat(String attributeId) {
        return "MMOCORE_" + UtilityMethods.enumName(attributeId);
    }

    //region Base points

    private int spent;

    public void setBase(int value) {
        final var effective = Math.max(0, value);
        if (effective == this.spent) return;

        spent = Math.max(0, value);
        updateBaseModifier();
    }

    public void addBase(int value) {
        setBase(this.spent + value);
    }

    public int getBase() {
        return spent;
    }

    private void updateBaseModifier() {
        // Very important - use the same UUID so that
        // it overrides the previous modifier
        if (this.spent == 0) getHandle().removeModifier(BASE_MODIFIER_UNIQUE_ID);
        else {
            var baseModifier = new StatModifier(BASE_MODIFIER_UNIQUE_ID, MODIFIER_KEY, this.mythicLibstatId, this.spent, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
            getHandle().registerModifier(baseModifier);
        }
    }

    //endregion

    public int getTotal() {
        return (int) getHandle().getTotal();
    }
}
