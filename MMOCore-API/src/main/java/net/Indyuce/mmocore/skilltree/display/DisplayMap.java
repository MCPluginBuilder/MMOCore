package net.Indyuce.mmocore.skilltree.display;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.util.IconOptions;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.Indyuce.mmocore.skilltree.PathState;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DisplayMap {
    public final Map<Object, IconOptions> icons = new HashMap<>();

    public static final IconOptions DEFAULT_ICON = new IconOptions(Material.BARRIER, 0);
    public static final DisplayMap EMPTY = new DisplayMap(new YamlConfiguration());

    private DisplayMap(@NotNull ConfigurationSection config) {

        // Loads all the pathDisplayInfo
        if (config.contains("paths")) for (var status : PathState.values())
            for (PathShape pathShape : PathShape.values())
                try {
                    final String configPath = "paths." + UtilityMethods.kebabCase(status.name() + "." + pathShape.name());
                    icons.put(new PathDisplayInfo(pathShape, status), IconOptions.from(config.get(configPath)));
                } catch (Exception exception) {
                    // Ignore
                }

        // Loads all the nodeDisplayInfo
        var nodeConfig = getNodeConfig(config);
        if (nodeConfig != null) for (var state : NodeState.values()) {
            final var statusConfig = nodeConfig.get(UtilityMethods.kebabCase(state.name()));
            if (statusConfig == null) continue;

            // Check if it depends on state
            if (statusConfig instanceof ConfigurationSection && UtilityMethods.containsOneKey((ConfigurationSection) statusConfig, NodeShape.values(), UtilityMethods::kebabCase))
                for (var shape : NodeShape.values()) {
                    try {
                        final var configPath = UtilityMethods.kebabCase(state.name() + "." + shape.name());
                        icons.put(new NodeDisplayInfo(shape, state), IconOptions.from(nodeConfig.get(configPath)));
                    } catch (Exception exception) {
                        // Ignore
                    }
                }

                // Depends on node type
            else {
                var iconFound = IconOptions.from(statusConfig);
                for (var shape : NodeShape.values())
                    icons.put(new NodeDisplayInfo(shape, state), iconFound);
            }
        }
    }

    @Nullable
    private ConfigurationSection getNodeConfig(@Nullable ConfigurationSection config) {

        // Null to null
        if (config == null) return null;

        // 'nodes' subconfig
        var subconfig = config.getConfigurationSection("nodes");
        if (subconfig != null) return subconfig;

        // Validate at least one state
        // Not strictly necessary.
        if (UtilityMethods.containsOneKey(config, NodeState.values(), UtilityMethods::kebabCase)) return config;

        // Wrong syntax..
        return null;
    }

    /**
     * @param nodeInfo Either a {@link NodeDisplayInfo} or {@link PathDisplayInfo}
     * @return Icon/texture mapping of node/path display info
     */
    @Nullable
    public static IconOptions getIcon(Object nodeInfo, @NotNull DisplayMap... maps) {
        int i = 0;
        IconOptions found = null;
        while (found == null && i < maps.length) found = maps[i++].icons.get(nodeInfo);
        return found;
    }

    @NotNull
    public static DisplayMap from(@Nullable ConfigurationSection config) {
        if (config == null) return EMPTY;
        return new DisplayMap(config);
    }
}
