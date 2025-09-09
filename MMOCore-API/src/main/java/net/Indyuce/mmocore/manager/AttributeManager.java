package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.config.YamlFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.MMOCoreAttributeStatHandler;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AttributeManager implements MMOCoreManager {
    private final Map<String, PlayerAttribute> map = new HashMap<>();

    @Nullable
    public PlayerAttribute get(String id) {
        return map.get(id);
    }

    @NotNull
    public PlayerAttribute getOrThrow(String id) {
        final var found = map.get(id);
        if (found == null)
            throw new IllegalArgumentException(String.format("Could not find attribute with ID '%s'", found));
        return found;
    }

    public boolean has(String id) {
        return map.containsKey(id);
    }

    @NotNull
    public Collection<PlayerAttribute> getAll() {
        return map.values();
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            map.clear();
            MythicLib.plugin.getStats().clearRegisteredStats(handler -> handler instanceof MMOCoreAttributeStatHandler);
        }

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "attributes", false, (key, config) -> {
            final String path = key.toLowerCase().replace("_", "-").replace(" ", "-");
            map.put(path, new PlayerAttribute(config));
        }, "Could not load attribute '%s' from file '%s': %s");

        final var statsConfig = new YamlFile(MythicLib.plugin, "stats").getContent();
        for (PlayerAttribute attr : getAll()) {
            final MMOCoreAttributeStatHandler handler = new MMOCoreAttributeStatHandler(statsConfig, attr);
            MythicLib.plugin.getStats().registerStat(handler, handler.getStat() + "_PERCENT");
        }
    }
}
