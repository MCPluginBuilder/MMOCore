package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Since attributes are just MythicLib stats, this
 * class is just a wrapper
 */
public class AttributeModifier extends StatModifier {
    private final PlayerAttribute attribute;

    public AttributeModifier(@NotNull UUID uniqueId, @NotNull String key, @NotNull PlayerAttribute attribute, double value, @NotNull ModifierType type, @NotNull EquipmentSlot slot, @NotNull ModifierSource source) {
        super(uniqueId, key, AttributeInstance.asMythicLibStat(attribute.getId()), value, type, slot, source);

        this.attribute = attribute;
    }

    @NotNull
    public PlayerAttribute getAttribute() {
        return attribute;
    }
}