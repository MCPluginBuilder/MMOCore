package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.stat.StatProxy;
import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.AttributeInstance;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AttributeManager implements MMOCoreManager {
    private final Map<String, PlayerAttribute> map = new HashMap<>();

    private final NamespacedKey attributeSource;

    public AttributeManager(MMOCore plugin) {
        attributeSource = new NamespacedKey(plugin, "attribute");
    }

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

    public boolean isMMOCoreAttribute(StatProxy proxy) {
        return proxy.getSource().equals(this.attributeSource);
    }

    public NamespacedKey getAttributeSource() {
        return attributeSource;
    }

    private void clearExistingProxies() {
        MythicLib.plugin.getStats().getHandlers().forEach(handler -> handler.getChildren().removeIf(this::isMMOCoreAttribute));
    }

    public void registerAttribute(@NotNull PlayerAttribute attribute) {

        // MMOCore registry
        if (map.containsKey(attribute.getId()))
            throw new IllegalArgumentException("Found existing attribute with ID '" + attribute.getId() + "'");
        map.put(attribute.getId(), attribute);

        // Add corresponding stat proxies to MythicLib
        var attributeStatId = AttributeInstance.asMythicLibStat(attribute.getId());
        var targetStatHandler = MythicLib.plugin.getStats().computeStat(attributeStatId);
        for (var proxy : attribute.getBuffs()) targetStatHandler.getChildren().add(proxy);

        // ADDITIONAL_xxxx stat
        // This is for backwards compatibility with MMOItems
        // TODO move backwards compatibility to MMOItems with a legacy NBT mapping or something
        var miBcStatId = "ADDITIONAL_" + attribute.getId().toUpperCase().replace("-", "_");
        var targetMiStatHandler = MythicLib.plugin.getStats().computeStat(miBcStatId);
        targetMiStatHandler.getChildren().add(new StatProxy(this.attributeSource, attributeStatId, ModifierType.FLAT, 1));
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            map.clear();
            this.clearExistingProxies();
        }

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "attributes",
                (key, config) -> registerAttribute(new PlayerAttribute(config)),
                "Could not load attribute '%s' from file '%s': %s");
    }
}
