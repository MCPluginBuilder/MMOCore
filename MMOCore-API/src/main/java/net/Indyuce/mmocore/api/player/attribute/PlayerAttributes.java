package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerAttributes {
    private final PlayerData data;
    private final StatMap statMap;
    private final Map<String, AttributeInstance> instances = new HashMap<>();

    public PlayerAttributes(PlayerData data) {
        this.data = data;
        this.statMap = data.getMMOPlayerData().getStatMap();
    }

    /**
     * When plugin is reloaded. Live references must be flushed
     */
    public void reload() {
        instances.values().forEach(AttributeInstance::flushReference);
    }

    /**
     * Save to YAML
     *
     * @param config Where to save the player attributes
     */
    public void save(@NotNull ConfigurationSection config) {
        for (var instance : instances.values()) {

            // Check if attribute is saved
            var attribute = instance.getAttribute();
            if (attribute != null && !attribute.isSaved()) continue;

            // Save to config
            config.set(instance.getAttributeId(), instance.getBase());
        }
    }

    /**
     * Save player attribute data to JSON
     *
     * @return JSON object
     */
    @NotNull
    public JsonObject toJson() {
        var json = new JsonObject();

        for (var instance : instances.values()) {

            // Check if attribute is saved
            var attribute = instance.getAttribute();
            if (attribute != null && !attribute.isSaved()) continue;

            json.addProperty(instance.getAttributeId(), instance.getBase());
        }

        return json;
    }

    @Deprecated
    public String toJsonString() {
        return toJson().toString();
    }

    /**
     * Load from JSON
     *
     * @param json String JSON object
     */
    public void load(@NotNull String json) {
        final var jo = MythicLib.plugin.getGson().fromJson(json, JsonObject.class);
        for (var entry : jo.entrySet())
            try {
                final String id = entry.getKey().toLowerCase().replace("_", "-").replace(" ", "-");
                final var attribute = MMOCore.plugin.attributeManager.get(id);
                Validate.notNull(attribute, "Could not find attribute called '" + id + "'");

                // [Backwards compatibility] Failsafe, ignore attributes that are not saved
                if (!attribute.isSaved()) continue;

                final AttributeInstance ins = new AttributeInstance(this.statMap, id);
                ins.setBase(entry.getValue().getAsInt());
                instances.put(id, ins);
            } catch (Exception exception) {
                data.log(Level.WARNING, "Could not load attribute '" + entry.getKey() + "', last value recorded is '" + entry.getValue() + "': " + exception.getMessage());
            }
    }

    /**
     * Load from YAML config section
     *
     * @param config YAML config section to load from
     */
    public void load(@NotNull ConfigurationSection config) {

        for (String key : config.getKeys(false))
            try {
                final var attributeId = key.toLowerCase().replace("_", "-").replace(" ", "-");
                final var attribute = MMOCore.plugin.attributeManager.getOrThrow(attributeId);

                // [Backwards compatibility] Failsafe, ignore attributes that are not saved
                if (!attribute.isSaved()) continue;

                final var ins = new AttributeInstance(this.statMap, attributeId);
                ins.setBase(config.getInt(key));
                instances.put(attributeId, ins);
            } catch (Exception exception) {
                data.log(Level.WARNING, exception.getMessage());
            }
    }

    public PlayerData getData() {
        return data;
    }

    public int getAttribute(PlayerAttribute attribute) {
        return getInstance(attribute).getTotal();
    }

    public int getAttribute(String attribute) {
        return getInstance(attribute).getTotal();
    }

    public Collection<AttributeInstance> getInstances() {
        return instances.values();
    }

    public Map<String, Integer> mapPoints() {
        var map = new HashMap<String, Integer>();
        instances.values().forEach(ins -> map.put(ins.getAttributeId(), ins.getBase()));
        return map;
    }

    @NotNull
    public AttributeInstance getInstance(String attribute) {
        return instances.computeIfAbsent(attribute, attributeId -> new AttributeInstance(this.statMap, attributeId));
    }

    @NotNull
    public AttributeInstance getInstance(PlayerAttribute attribute) {
        return getInstance(attribute.getId());
    }

    @Deprecated
    public int countSkillPoints() {
        return countPoints();
    }

    public int countPoints() {
        int n = 0;
        for (AttributeInstance ins : instances.values())
            n += ins.getBase();
        return n;
    }

    @Deprecated
    public void setBaseAttribute(String id, int value) {
        AttributeInstance ins = instances.get(id);
        if (ins != null) ins.setBase(value);
    }
}
