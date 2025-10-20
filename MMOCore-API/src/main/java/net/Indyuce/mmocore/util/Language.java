package net.Indyuce.mmocore.util;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public enum Language {
    EXP_HOLOGRAM,
    CAUGHT_FISH,
    GOLD_POUCH_UI_NAME,
    FISH_OUT_WATER,
    FISH_OUT_WATER_CRIT,
    NO_SKILL_PLACEHOLDER;

    private final String path;

    @NotNull
    private String translation = "";

    Language() {
        this.path = UtilityMethods.kebabCase(name());
    }

    @NotNull
    public String getFormat() {
        return translation;
    }

    public static void loadLanguageFromConfig(@NotNull ConfigurationSection config) {

        for (var element : values())
            try {
                final var objectFound = config.getString(element.path);
                Validate.notNull(objectFound, "Entry not found");
                element.translation = objectFound;
            } catch (Exception exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load language entry '" + element.path + "': " + exception.getMessage());
                element.translation = "<translation_entry_not_found>";
            }

    }
}
