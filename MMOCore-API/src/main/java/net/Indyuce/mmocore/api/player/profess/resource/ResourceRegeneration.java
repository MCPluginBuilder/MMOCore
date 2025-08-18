package net.Indyuce.mmocore.api.player.profess.resource;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.util.formula.FormulaFailsafeException;
import net.Indyuce.mmocore.util.formula.ScalingFormula;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.BiFunction;

public class ResourceRegeneration {

    /**
     * Resource should only regenerate when the player is out of combat
     */
    private final boolean offCombatOnly;

    /**
     * Percentage of scaling which the player regenerates every second
     */
    private final ScalingFormula scalar;

    /**
     * Whether the resource regeneration scales on missing or max resource. if
     * TYPE is null, then there is no special regeneration.
     */
    private final HandlerType type;
    private final PlayerResource resource;

    /**
     * Used when there is no special resource regeneration
     */
    public ResourceRegeneration(PlayerResource resource) {
        this(resource, null, null, false);
    }

    public ResourceRegeneration(PlayerResource resource, ConfigurationSection config) {
        this.resource = resource;
        offCombatOnly = config.getBoolean("off-combat");

        Validate.isTrue(config.contains("type"), "Could not find scaling type");
        type = HandlerType.valueOf(config.getString("type").toUpperCase());

        Validate.notNull(config.get("value"), "Could not find regen value");
        scalar = ScalingFormula.fromConfig(config.get("value"));
    }

    public ResourceRegeneration(PlayerResource resource, HandlerType type, ScalingFormula scalar, boolean offCombatOnly) {
        this.resource = resource;
        this.type = type;
        this.scalar = scalar;
        this.offCombatOnly = offCombatOnly;
    }

    /**
     * Apply regeneration formulas: first calculates base resource regen due to
     * the player stats and then apply the special resource regeneration due to
     * the player class
     *
     * @param player Player regenerating
     * @return The amount of resource which should be regenerated EVERY SECOND
     */
    public double getRegen(PlayerData player) {
        double d = 0;

        if (!player.isInCombat() || !player.getProfess().hasOption(resource.getOffCombatRegen())) {

            // Flat resource regeneration
            d += player.getStats().getStat(resource.getRegenStat());

            // Component which scales with max resource
            d += player.getStats().getStat(resource.getMaxRegenStat()) / 100 * resource.getMax(player);
        }

        // Special resource regeneration
        if (type != null && (!player.isInCombat() || !offCombatOnly)) try {
            d += this.scalar.evaluate(player.getLevel(), player) / 100 * type.getScaling(player, resource);
        } catch (FormulaFailsafeException exception) {
            exception.log("Could not evaluate special resource %s regen value for class %s", resource.name(), player.getProfess().getId());
        }

        return d;
    }

    public enum HandlerType {

        /**
         * Resource regeneration scales on max resource
         */
        MAX((player, resource) -> resource.getMax(player)),

        /**
         * Resource regeneration scales on missing resource
         */
        MISSING((player, resource) -> resource.getMax(player) - resource.getCurrent(player));

        private final BiFunction<PlayerData, PlayerResource, Double> calculation;

        HandlerType(BiFunction<PlayerData, PlayerResource, Double> calculation) {
            this.calculation = calculation;
        }

        public double getScaling(PlayerData player, PlayerResource resource) {
            return calculation.apply(player, resource);
        }
    }
}
