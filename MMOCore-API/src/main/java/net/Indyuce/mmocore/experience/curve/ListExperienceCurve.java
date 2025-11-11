package net.Indyuce.mmocore.experience.curve;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ListExperienceCurve implements ExperienceCurve {

    /**
     * Experience needed to level up. At index i, experience needed
     * to level up from level N to (N + 1).
     */
    private final List<Long> experience;

    /**
     * Reads an exp curve from a text file, one line after the other. Each
     * exp value has to be the only thing written on every line
     *
     * @param file Text file to read data from
     */
    public ListExperienceCurve(@NotNull File file) {
        this.experience = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = reader.readLine()) != null)
                experience.add(Long.valueOf(readLine));
            reader.close();

            Validate.isTrue(!experience.isEmpty(), "There must be at least one exp value in your exp curve");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Exp curve with specific level up exp values
     *
     * @param values The exp values. There has to be at least one
     */
    public ListExperienceCurve(long... values) {
        this.experience = new ArrayList<>(values.length);
        for (long value : values) experience.add(value);
        Validate.isTrue(!experience.isEmpty(), "There must be at least one exp value in your exp curve");
    }

    @Override
    public long getExperience(@NotNull PlayerData player, int level) {
        Validate.isTrue(level > 0, "Level must be strictly positive");
        return experience.get(Math.min(level, experience.size()) - 1);
    }
}
