package net.Indyuce.mmocore.util.formula;

import net.Indyuce.mmocore.MMOCore;

import java.util.logging.Level;

public class FormulaFailsafeException extends RuntimeException {
    private final double failsafe;

    public FormulaFailsafeException(Exception thrown, double failsafe) {
        super(thrown);

        this.failsafe = failsafe;
    }

    public double getFailsafe() {
        return failsafe;
    }

    public void log(String format, Object... params) {
        MMOCore.plugin.getLogger().log(Level.WARNING, String.format(format, params) + ": " + getCause().getMessage());
    }
}
