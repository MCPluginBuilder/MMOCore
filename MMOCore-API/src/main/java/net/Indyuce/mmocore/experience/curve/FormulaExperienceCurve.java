package net.Indyuce.mmocore.experience.curve;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.util.expression.placeholder.ExpressionPlaceholder;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

public class FormulaExperienceCurve implements ExperienceCurve {
    private final NumericExpression formula;

    public FormulaExperienceCurve(@NotNull String formula) {
        this.formula = NumericExpression.compile(formula, this::parseCustomPlaceholders);
    }

    private ExpressionPlaceholder parseCustomPlaceholders(@NotNull String placeholder) {
        if (placeholder.equals("level")) return new LevelPlaceholder();
        throw new IllegalArgumentException("Unknown placeholder {" + placeholder + "}, only supports {level}");
    }

    static class LevelPlaceholder implements ExpressionPlaceholder {

        @Override
        public Double parse(@NotNull SkillMetadata skillMetadata) {
            var player = PlayerData.get(skillMetadata.getCaster().getData().getUniqueId());
            return (double) player.getLevel();
        }
    }

    @Override
    public long getExperience(@NotNull PlayerData player, int level) {
        try {
            Validate.isTrue(level > 0, "Level must be stricly positive, got " + level);
            final int value = (int) this.formula.evaluate(SkillMetadata.of(player.getMMOPlayerData()));
            Validate.isTrue(value > 0, "Exp curve must return a positive value, got " + value);
            return value;

        } catch (Exception e) {
            MythicLib.plugin.getLogger().warning("Error parsing exp formula '" + this.formula + "' for player " + player.getPlayer().getName() + ", using 100: " + e.getMessage());
            return 100;
        }
    }
}
