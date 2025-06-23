package net.Indyuce.mmocore.skill.cast;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Keybind {

    /**
     * Key that must be pressed to match the keybind.
     */
    private final PlayerKey key;

    /**
     * If true, player must be crouching.
     * If false, player must not be crouching.
     * If null, crouching does not matter.
     */
    private final Boolean sneak;

    public Keybind(Object object) {

        // `sneak` defaults to null
        if (object instanceof String) {
            key = PlayerKey.valueOf(UtilityMethods.enumName((String) object));
            sneak = null;
        } else if (object instanceof ConfigurationSection) {
            var config = (ConfigurationSection) object;
            key = PlayerKey.valueOf(UtilityMethods.enumName(config.getString("key", "NONE")));
            sneak = config.contains("sneak") ? config.getBoolean("sneak") : null;
        }

        // Invalid syntax
        else throw new IllegalArgumentException("Keybind requires a string or config section");
    }

    public Keybind(PlayerKey key, Boolean sneak) {
        this.key = key;
        this.sneak = sneak;
    }

    public boolean matches(@NotNull PlayerKeyPressEvent event) {
        return event.getPressed() == key && (sneak == null || event.getPlayer().isSneaking() == sneak);
    }

    @Nullable
    public static Keybind fromConfig(@Nullable Object object) {
        if (object == null) return null;
        return new Keybind(object);
    }
}
