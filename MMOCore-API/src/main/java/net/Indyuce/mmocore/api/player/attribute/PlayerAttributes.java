package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.gson.JsonElement;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class PlayerAttributes {
    private final PlayerData data;
    private final Map<String, AttributeInstance> instances = new HashMap<>();

    public PlayerAttributes(PlayerData data) {
        this.data = data;
    }

    /**
     * When plugin is reloaded. Live references must be flushed
     */
    public void reload() {
        instances.values().forEach(ins -> ins.attribute.flush());
    }

    /**
     * Save to YAML
     *
     * @param config Where to save the player attributes
     */
    public void save(@NotNull ConfigurationSection config) {
        for (var instance : instances.values()) {

            // Check if attribute is saved
            var attribute = instance.attribute.get();
            if (!attribute.isSaved()) continue;

            // Save to config
            config.set(instance.getId(), instance.getBase());
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
            var attribute = instance.attribute.get();
            if (!attribute.isSaved()) continue;

            json.addProperty(instance.getId(), instance.getBase());
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
        JsonObject jo = MythicLib.plugin.getGson().fromJson(json, JsonObject.class);
        for (Entry<String, JsonElement> entry : jo.entrySet()) {
            try {
                final String id = entry.getKey().toLowerCase().replace("_", "-").replace(" ", "-");
                final var attribute = MMOCore.plugin.attributeManager.get(id);
                Validate.notNull(attribute, "Could not find attribute called '" + id + "'");

                // [Backwards compatibility] Failsafe, ignore attributes that are not saved
                if (!attribute.isSaved()) continue;

                final AttributeInstance ins = new AttributeInstance(id);
                ins.setBase(entry.getValue().getAsInt());
                instances.put(id, ins);
            } catch (IllegalArgumentException exception) {
                data.log(Level.WARNING, exception.getMessage());
            }
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

                final var ins = new AttributeInstance(attributeId);
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

    public Collection<AttributeInstance> getInstances() {
        return instances.values();
    }

    public Map<String, Integer> mapPoints() {
        Map<String, Integer> map = new HashMap<>();
        instances.values().forEach(ins -> map.put(ins.id, ins.spent));
        return map;
    }

    @NotNull
    public AttributeInstance getInstance(String attribute) {
        return instances.computeIfAbsent(attribute, AttributeInstance::new);
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

    // TODO have it extend ModifiedInstance?
    // TODO not do that before the general "archetype" MMOCore update
    public class AttributeInstance {
        private int spent;

        private final String id, enumName;

        /**
         * Using a lazy value allows to flush values. When MMOCore
         * is reloaded, references to dead instances of player attributes
         * remain in attribute instances and need to be flushed.
         */
        private final Lazy<PlayerAttribute> attribute;

        private final Map<String, AttributeModifier> map = new HashMap<>();

        public AttributeInstance(@NotNull String attributeId) {
            this.id = attributeId;
            this.enumName = UtilityMethods.enumName(this.id);
            this.attribute = Lazy.persistent(() -> MMOCore.plugin.attributeManager.get(this.id));
        }

        /**
         * @return ID of corresponding attribute
         */
        public String getId() {
            return id;
        }

        public int getBase() {
            return spent;
        }

        @Deprecated
        public int getSpent() {
            return getBase();
        }

        public void setBase(int value) {
            spent = Math.max(0, value);

            if (data.isOnline())
                updateStats();
        }

        public void addBase(int value) {
            setBase(getBase() + value);
        }

        /*
         * 1) two types of attributes: flat attributes which add X to the value,
         * and relative attributes which add X% and which must be applied
         * afterwards 2) the 'd' parameter lets you choose if the relative
         * attributes also apply on the base stat, or if they only apply on the
         * instances stat value
         */
        public int getTotal() {
            double d = spent;

            for (AttributeModifier attr : map.values())
                if (attr.getType() == ModifierType.FLAT)
                    d += attr.getValue();

            d += data.getMMOPlayerData().getStatMap().getStat("ADDITIONAL_" + enumName);

            for (AttributeModifier attr : map.values())
                if (attr.getType() == ModifierType.RELATIVE)
                    d *= attr.getValue();

            d *= 1 + data.getMMOPlayerData().getStatMap().getStat("ADDITIONAL_" + enumName + "_PERCENT") / 100;

            // cast to int at the last moment
            return (int) d;
        }

        public AttributeModifier getModifier(String key) {
            return map.get(key);
        }

        public AttributeModifier addModifier(String key, double value) {
            return addModifier(new AttributeModifier(key, id, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));
        }

        public AttributeModifier addModifier(AttributeModifier modifier) {
            final AttributeModifier current = map.put(modifier.getKey(), modifier);

            if (current instanceof Closeable) ((Closeable) current).close();

            updateStats();
            return current;
        }

        public Set<String> getKeys() {
            return map.keySet();
        }

        public boolean contains(String key) {
            return map.containsKey(key);
        }

        public AttributeModifier removeModifier(String key) {
            final AttributeModifier mod = map.remove(key);

            /*
             * Closing stat is really important with temporary stats because
             * otherwise the runnable will try to remove the key from the map
             * even though the attribute was cancelled before hand
             */
            if (mod != null) {
                if (mod instanceof Closeable) ((Closeable) mod).close();
                updateStats();
            }
            return mod;
        }

        public void updateStats() {
            final var total = getTotal();

            // Remove ALL stat modifiers
            for (var ins : data.getMMOPlayerData().getStatMap().getInstances())
                ins.removeIf(str -> str.equals("attribute." + id));

            // Register new stat modifiers
            attribute.get().getBuffs().forEach(buff -> buff.multiply(total).register(data.getMMOPlayerData()));
        }
    }

    @Deprecated
    public void setBaseAttribute(String id, int value) {
        AttributeInstance ins = instances.get(id);
        if (ins != null) ins.setBase(value);
    }
}
