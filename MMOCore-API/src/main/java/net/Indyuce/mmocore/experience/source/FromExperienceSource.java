package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;

import java.util.ArrayList;
import java.util.List;

public class FromExperienceSource extends ExperienceSource {

    /**
     * Register all the children experience sources defined in experience-source.yml.
     */
    private final List<ExperienceSource> experienceSources = new ArrayList<>();

    public FromExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        config.validateKeys("source");
        var list = new YamlFile(MMOCore.plugin, "exp-sources").getContent().getStringList(config.getString("source"));
        Validate.isTrue(list != null && !list.isEmpty(), "There is no source matching " + config.getString("source"));
        list.stream()
                .map(MMOLineConfig::new)
                .forEach(mmoLineConfig ->
                        experienceSources.add(MMOCore.plugin.loadManager.loadExperienceSource(mmoLineConfig, dispenser)));
    }

    @Override
    public ExperienceSourceManager<FromExperienceSource> newManager() {
        return new Manager();
    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        return false;
    }

    private static class Manager extends ExperienceSourceManager<FromExperienceSource> {

        /**
         * Used to register all the children experience sources.
         */
        @Override
        public void registerSource(FromExperienceSource source) {
            source.experienceSources.forEach(expSource -> MMOCore.plugin.experience.registerSource(expSource));
        }
    }
}
