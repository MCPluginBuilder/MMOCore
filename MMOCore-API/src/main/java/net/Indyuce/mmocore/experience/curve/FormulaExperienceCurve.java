package net.Indyuce.mmocore.experience.curve;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

public class FormulaExperienceCurve implements ExperienceCurve {
    private final String formula;

    public FormulaExperienceCurve(@NotNull String formula) {
        this.formula = formula;
    }

    @Override
    public long getExperience(@NotNull PlayerData player, int level) {
        try {
            Validate.isTrue(level > 0, "Level must be stricly positive, got " + level);
            final var parsed = MythicLib.plugin.getPlaceholderParser().parse(player.getPlayer(), this.formula);
            final var value = (int) NumericalExpression.eval(parsed.replace("{level}", String.valueOf(level)));
            Validate.isTrue(value > 0, "Exp curve must return a positive value, got " + value);
            return value;

        } catch (Exception e) {
            MythicLib.plugin.getLogger().warning("Error parsing exp formula '" + this.formula + "' for player " + player.getPlayer().getName() + ", using 100: " + e.getMessage());
            return 100;
        }
    }
}
