package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.curve.ExperienceCurve;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.jetbrains.annotations.NotNull;

/**
 * General implementation for professions, classes and attributes.
 * <p>
 * An experience object is a type of object that can level up.
 * It has an experience curve and table and can receive EXP. It
 * is what most resembles the Mythic abstraction of "archetypes".
 *
 * @author jules
 */
public interface ExperienceObject extends ExperienceDispenser {

    @NotNull
    String getKey();

    /**
     * Indicates the amount of exp required to level up
     *
     * @return Exp curve of that object
     */
    @NotNull
    ExperienceCurve getExpCurve();

    /**
     * Throws an exception no experience table
     *
     * @return Table read when leveling up
     * @see #hasExperienceTable()
     */
    @NotNull
    ExperienceTable getExperienceTable();

    boolean hasExperienceTable();

    /**
     * Resets the advancement of an archetype for a player. This only
     * applies to the object's experience table though, and does not
     * actually decrease class level/profession level & exp bar.
     */
    public default void resetAdvancement(@NotNull PlayerData playerData, boolean levels) {
        if (hasExperienceTable()) getExperienceTable().unclaim(playerData, this, levels);
    }

    public default void updateAdvancement(@NotNull PlayerData playerData, int newLevel) {
        if (hasExperienceTable()) getExperienceTable().claim(playerData, newLevel, this);
    }
}
