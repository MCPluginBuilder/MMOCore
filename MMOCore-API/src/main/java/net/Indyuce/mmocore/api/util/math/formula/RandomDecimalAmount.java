package net.Indyuce.mmocore.api.util.math.formula;

import io.lumine.mythic.lib.util.lang3.Validate;

import java.util.Random;

public class RandomDecimalAmount {
    private final double min, max;
    private final boolean hasMax;

    public RandomDecimalAmount(double constant) {
        this.min = constant;
        this.max = constant;
        this.hasMax = false;
    }

    public RandomDecimalAmount(double min, double max) {
        this.min = min;
        this.max = max;
        Validate.isTrue(max > min, "Max must be greater than min");
        this.hasMax = true;
    }

    public RandomDecimalAmount(String value) {
        var split = value.split("-");
        min = Double.parseDouble(split[0]);
        if (split.length > 1) {
            max = Double.parseDouble(split[1]);
            hasMax = max <= min; // Not logical, but backwards compatible
        } else {
            max = min;
            hasMax = false;
        }
    }

    public double roll() {
        if (!hasMax) return min;
        return min + Math.random() * (max - min);
    }
}
