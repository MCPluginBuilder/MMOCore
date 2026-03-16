package net.Indyuce.mmocore.experience.curve;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.util.expression.placeholder.ExpressionPlaceholder;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            return (double) ((DummySkillMetadata) skillMetadata).playerData.getLevel();
        }
    }

    @Override
    public long getExperience(@NotNull PlayerData player, int level) {
        try {
            Validate.isTrue(level > 0, "Level must be stricly positive, got " + level);
            var value = (int) this.formula.evaluate(new DummySkillMetadata(player));
            Validate.isTrue(value > 0, "Exp curve must return a positive value, got " + value);
            return value;

        } catch (Exception exception) {
            MythicLib.plugin.getLogger().warning("Error parsing exp formula '" + this.formula + "' for player " + player.getMMOPlayerData().getPlayerName() + ", using 100: " + exception.getMessage());
            return 100;
        }
    }

    @Deprecated
    static class DummySkillMetadata extends SkillMetadata {
        final PlayerData playerData;

        @Deprecated
        public DummySkillMetadata(@NotNull PlayerData lookup) {
            super(null,
                    new PlayerMetadata(lookup.getMMOPlayerData()),
                    new VariableList(VariableScope.SKILL),
                    __getDummyLocation(),
                    null, null, null, null, null);

            this.playerData = lookup;
        }

        @Deprecated
        private static Location __getDummyLocation() {
            for (var world : Bukkit.getWorlds())
                return world.getSpawnLocation();
            throw new RuntimeException("No world?");
        }
    }
}
