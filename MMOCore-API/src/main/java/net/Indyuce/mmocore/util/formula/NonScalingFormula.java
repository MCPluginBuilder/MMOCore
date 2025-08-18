package net.Indyuce.mmocore.util.formula;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.Nullable;

public class NonScalingFormula implements ScalingFormula {
    private final double constant;

    @Override
    public boolean isInteger() {
        return false;
    }

    public NonScalingFormula(double constant) {
        this.constant = constant;
    }

    @Override
    public double evaluate(int skillLevel, @Nullable PlayerData playerData) {
        return constant;
    }
}
