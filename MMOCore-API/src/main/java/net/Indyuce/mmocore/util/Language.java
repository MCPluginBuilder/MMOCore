package net.Indyuce.mmocore.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
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
    private String translation = TRANSLATION_NOT_FOUND;

    private static final String TRANSLATION_NOT_FOUND = "<translation_entry_not_found>";

    Language(String path) {
        this.path = path;
    }

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
                element.translation = TRANSLATION_NOT_FOUND;
            }
    }
}
