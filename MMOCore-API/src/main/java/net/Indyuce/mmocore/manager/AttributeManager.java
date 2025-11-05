package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
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
        if (clearBefore)
            map.clear();

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "attributes", false, (key, config) -> {
            final String path = key.toLowerCase().replace("_", "-").replace(" ", "-");
            map.put(path, new PlayerAttribute(config));
        }, "Could not load attribute '%s' from file '%s': %s");

        // MythicLib stat handlers
        for (PlayerAttribute attr : getAll()) {
            final var statId = attr.getId().toUpperCase().replace("-", "_") + "_PERCENT";
            MythicLib.plugin.getStats().computeStat(statId).addUpdateListener(ins -> this.updateMMOCoreStatAttributeValue(ins, attr));
        }
    }

    private void updateMMOCoreStatAttributeValue(@NotNull StatInstance instance, PlayerAttribute attribute) {
        final var playerData = PlayerData.get(instance.getMap().getPlayerData());
        playerData.getAttributes().getInstance(attribute).updateStats();
    }
}
