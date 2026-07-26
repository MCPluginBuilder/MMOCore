package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.stat.StatProxy;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.curve.ExperienceCurve;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class PlayerAttribute implements ExperienceObject {
    private final String id, name;
    private final int max;
    private final ExperienceTable expTable;
    private final boolean save;

    /**
     * All buffs granted by an attribute. These are normalized and
     * must be multiplied by the player level first
     */
    private final List<StatProxy> buffs = new ArrayList<>();

    public PlayerAttribute(ConfigurationSection config) {
        Validate.notNull(config, "Could not load config");
        id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

        name = MythicLib.plugin.parseColors(config.getString("name", "MyAttribute"));
        max = config.contains("max-points") ? Math.max(1, config.getInt("max-points")) : 0;
        save = config.getBoolean("save-to-player-data", true);

        if (config.contains("buff"))
            for (String key : config.getConfigurationSection("buff").getKeys(false))
                try {
                    var statProxy = statProxyFromConfig(key, config.getString("buff." + key));
                    this.buffs.add(statProxy);
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load buff '" + key + "' from attribute '" + id + "': " + exception.getMessage());
                }

        // Load exp table
        ExperienceTable expTable = null;
        if (config.contains("exp-table"))
            try {
                expTable = MMOCore.plugin.experience.loadExperienceTable(config.get("exp-table"));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load exp table from attribute '" + id + "': " + exception.getMessage());
            }
        this.expTable = expTable;
    }

    @NotNull
    private StatProxy statProxyFromConfig(String configKey, String configValue) {
        var proxyInfo = ModifierType.pairFromString(configValue);
        var targetStat = UtilityMethods.enumName(configKey);
        return new StatProxy(MMOCore.plugin.attributeManager.getAttributeSource(), targetStat, proxyInfo.getLeft(), proxyInfo.getRight());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasMax() {
        return max > 0;
    }

    public int getMax() {
        return max;
    }

    public boolean isSaved() {
        return save;
    }

    public List<StatProxy> getBuffs() {
        return buffs;
    }

    @Override
    public @NotNull String getKey() {
        return "attribute:" + getId().replace("-", "_");
    }

    @NotNull
    @Override
    public ExperienceTable getExperienceTable() {
        return Objects.requireNonNull(expTable, "Attribute has no exp table");
    }

    @Override
    public boolean hasExperienceTable() {
        return expTable != null;
    }

    @NotNull
    @Override
    public ExperienceCurve getExpCurve() {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, @NotNull EXPSource source) {
        throw new RuntimeException("Attributes don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Attributes don't have experience");
    }
}
