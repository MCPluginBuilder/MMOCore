package net.Indyuce.mmocore.experience.curve;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExperienceCurve {

    /**
     * Arbitrary values. MMOCore needs a default exp curve for everything otherwise
     * it would be swarming be divisions by 0 when trying to update the vanilla
     * exp bar which requires a 0.0 -> 1.0 float as parameter.
     * <p>
     * See {@link PlayerData#refreshVanillaExp()}
     */
    public static final ExperienceCurve DEFAULT = new ListExperienceCurve(100, 200, 300, 400, 500);

    /**
     * If 1 is provided as level, this method returns the experience needed
     * to reach level 2. If the exp curve is a formula, it will provide 1 to the formula.
     * If the exp curve is a list, it will return the first element of the list.
     *
     * @param player Player leveling up
     * @param level  Current level of the player.
     * @return Experience needed to reach the next level.
     */
    public long getExperience(@NotNull PlayerData player, int level);

    @NotNull
    public static ExperienceCurve fromConfig(@Nullable String configInput) {

        if (configInput == null) {
            return DEFAULT;
        }

        // Is it an exp curve ID?
        try {
            return MMOCore.plugin.experience.getCurveOrThrow(configInput);
        } catch (Exception exception) {
            // Ignore
        }

        // Load as formula
        return new FormulaExperienceCurve(configInput);
    }
}
