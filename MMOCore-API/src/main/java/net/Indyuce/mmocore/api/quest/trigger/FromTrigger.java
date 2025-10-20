package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class FromTrigger extends Trigger {
    private final List<Trigger> triggers = new ArrayList<>();

    public FromTrigger(MMOLineConfig config) {
        super(config);

        var list = new YamlFile(MMOCore.plugin, "triggers").getContent().getStringList(config.getString("source"));
        Validate.isTrue(!list.isEmpty(), "There is no source matching " + config.getString("key"));
        list.stream().map(MMOLineConfig::new)
                .forEach(mmoLineConfig ->
                        triggers.add(MMOCore.plugin.loadManager.loadTrigger(mmoLineConfig)));

    }

    /**
     * Applies the effect of all the children triggers defined in triggers.yml.
     */
    @Override
    public void apply(PlayerData player) {
        triggers.forEach(trigger -> trigger.apply(player));
    }
}
