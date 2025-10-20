package net.Indyuce.mmocore.skilltree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.gui.util.IconOptions;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.ExpCurve;
import net.Indyuce.mmocore.experience.ExperienceObject;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.skilltree.display.DisplayMap;
import net.Indyuce.mmocore.skilltree.display.NodeDisplayInfo;
import net.Indyuce.mmocore.skilltree.display.NodeShape;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

// We must use generics to get the type of the corresponding tree

public class SkillTreeNode implements ExperienceObject {
    private final SkillTree tree;
    private final String name, id;
    private final String permissionRequired;
    private final int pointConsumption;
    private final DisplayMap icons;
    private final IntCoords coordinates;
    private final int maxLevel, maxChildren;
    private final ExperienceTable experienceTable;
    private final List<ParentInformation> children = new ArrayList<>();
    private final List<ParentInformation> parents = new ArrayList<>();
    private final Map<Integer, List<String>> lores = new HashMap<>();

    private boolean root;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {

        // Load children
        // Requires other nodes to be loaded first
        loadRelatives(true, config);

        // Load parents. Both work, one way or the other
        // Requires other nodes to be loaded first
        loadRelatives(false, config);
    });

    private void loadRelatives(boolean nodeIsParent, @NotNull ConfigurationSection config) {
        final var configPath = nodeIsParent ? "children" : "parents";

        if (config.isConfigurationSection(configPath))
            for (var parentTypeRaw : config.getConfigurationSection(configPath).getKeys(false)) {
                final var section = config.getConfigurationSection(configPath + "." + parentTypeRaw);
                Validate.notNull(section, "Could not read " + configPath + " of type '" + parentTypeRaw + "'");
                final ParentType parentType = UtilityMethods.prettyValueOf(ParentType::valueOf, parentTypeRaw, "No parent type called '%s'");

                for (var relativeId : section.getKeys(false)) {
                    final var relative = SkillTreeNode.this.tree.getNode(relativeId);
                    final var child = nodeIsParent ? relative : this;
                    final var parent = nodeIsParent ? this : relative;
                    child.addParent(ParentInformation.fromConfig(child, parent, parentType, section.get(relativeId)));
                }
            }
    }

    public SkillTreeNode(@NotNull SkillTree tree, @NotNull ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        this.id = config.getName();
        this.tree = tree;

        postLoadAction.cacheConfig(config);

        // Load icons for node states
        this.icons = DisplayMap.from(config.getConfigurationSection("display"));

        name = Objects.requireNonNull(config.getString("name"), "Could not find node name");
        root = config.getBoolean("root", config.getBoolean("is-root")); // backwards compatibility
        pointConsumption = config.getInt("point-consumed", 1);
        permissionRequired = config.getString("permission-required");
        Validate.isTrue(pointConsumption > 0, "The skill tree points consumed by a node must be greater than 0.");
        if (config.contains("lores"))
            for (String key : config.getConfigurationSection("lores").getKeys(false))
                try {
                    lores.put(Integer.parseInt(key), config.getStringList("lores." + key));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException("You shall only specify integers in the 'lores' config section");
                }

        try {
            Validate.isTrue(config.contains("experience-table"), "You must specify an exp table");
            this.experienceTable = MMOCore.plugin.experience.loadExperienceTable(config.get("experience-table"));
        } catch (RuntimeException exception) {
            throw new RuntimeException("Could not load experience table: " + exception.getMessage());
        }

        maxLevel = config.getInt("max-level", 1);
        Validate.isTrue(maxLevel > 0, "Max level must be positive");
        maxChildren = config.getInt("max-children", 0);
        Validate.isTrue(maxChildren >= 0, "Max children must positive or zero");
        coordinates = IntCoords.from(config.get("coordinates"));
    }

    public SkillTree getTree() {
        return tree;
    }

    public boolean isRoot() {
        return root;
    }

    @NotNull
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    /**
     * Registers this relation both as a parent and child relation in the right
     * registers of the parent and child nodes.
     * <p>
     * Note that the {@link #children} and {@link #parents} maps are only
     * modified through this method.
     */
    public void addParent(@NotNull ParentInformation parentInfo) {
        Validate.isTrue(parentInfo.getChild().equals(this), "#addParent(..) must be called on child node");

        parents.add(parentInfo);
        parentInfo.getParent().children.add(parentInfo);
    }

    public void setRoot() {
        root = true;
    }

    public int getPointConsumption() {
        return pointConsumption;
    }

    public boolean hasParent(SkillTreeNode parent) {
        for (var edge : parents)
            if (edge.getParent().equals(parent)) return true;
        return false;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public boolean hasPermissionRequirement(@NotNull PlayerData playerData) {
        return permissionRequired == null || playerData.getPlayer().hasPermission(permissionRequired);
    }

    @NotNull
    public List<ParentInformation> getParents() {
        return parents;
    }

    @NotNull
    public List<ParentInformation> getChildren() {
        return children;
    }

    /**
     * @return The node identifier relative to its skill tree, like "extra_strength"
     */
    public String getId() {
        return id;
    }

    /**
     * @return Full node identifier, containing both the node identifier AND
     *         the skill tree identifier, like "combat_extra_strength"
     */
    @NotNull
    public String getFullId() {
        return tree.getId() + "_" + id;
    }

    @NotNull
    public String getName() {
        return MythicLib.plugin.parseColors(name);
    }

    @NotNull
    public IntCoords getCoordinates() {
        return coordinates;
    }

    @NotNull
    public DisplayMap getIcons() {
        return icons;
    }

    public static final String KEY_PREFIX = "node";

    @Override
    public String getKey() {
        return KEY_PREFIX + ":" + getFullId().replace("-", "_");
    }

    @Override
    @NotNull
    public ExperienceTable getExperienceTable() {
        return Objects.requireNonNull(experienceTable, "Skill tree has no exp table");
    }

    @Override
    public boolean hasExperienceTable() {
        return experienceTable != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTreeNode that = (SkillTreeNode) o;
        return tree.equals(that.tree) && (id.equals(that.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(tree, id);
    }

    public List<String> getLore(PlayerData playerData) {
        final int nodeLevel = playerData.getNodeLevel(this);
        final List<String> parsedLore = new ArrayList<>();

        for (int i = nodeLevel; i >= 0; i--) {
            final List<String> found = lores.get(i);
            if (found == null) continue;

            final Placeholders holders = getPlaceholders(playerData);
            found.forEach(string -> parsedLore.add(MythicLib.plugin.parseColors(holders.apply(playerData.getPlayer(), string))));
            break;
        }

        return parsedLore;
    }

    private Placeholders getPlaceholders(@NotNull PlayerData playerData) {
        Placeholders holders = new Placeholders();
        holders.register("name", getName());
        holders.register("node-state", playerData.getNodeState(this));
        holders.register("level", playerData.getNodeLevel(this));
        holders.register("max-level", getMaxLevel());
        holders.register("max-children", getMaxChildren());
        return holders;
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation,
                               @NotNull EXPSource source) {
        throw new RuntimeException("Skill trees don't have experience");
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        throw new RuntimeException("Skill trees don't have experience");
    }

    @Nullable
    @Override
    public ExpCurve getExpCurve() {
        throw new RuntimeException("Skill trees don't have experience");
    }

    //region Deprecated

    @Deprecated
    public void loadLegacyPathSection(@NotNull ConfigurationSection section) {
        for (var childId : section.getKeys(false)) {
            final var child = tree.getNode(childId);
            Validate.notNull(child, "Could not find child node '" + childId + "' for path");

            // Find corresponding edge
            ParentInformation edge = null;
            for (var existingEdge : children)
                if (existingEdge.getChild().equals(child)) {
                    edge = existingEdge;
                    break;
                }
            if (edge == null) {
                MMOCore.log("Could not find parent-child relation between '" + id + "' and '" + childId + "'. Did you forget to add it in the 'parents' or 'children' section?");
                continue;
            }

            final var subsection = section.getConfigurationSection(childId);
            for (var pathKey : subsection.getKeys(false)) {
                final var coords = IntCoords.from(subsection.get(pathKey));
                edge.addElement(coords);
            }
        }
    }

    @Deprecated
    public int getParentNeededLevel(SkillTreeNode parent) {
        for (var edge : parents)
            if (edge.getParent().equals(parent)) return edge.getLevel();
        throw new RuntimeException("Could not find parent " + parent.getId() + " for node " + id);
    }

    @NotNull
    @Deprecated
    public List<SkillTreeNode> getParents(ParentType parentType) {
        return parents.stream().filter(integer -> integer.getType() == parentType).map(ParentInformation::getParent).collect(Collectors.toList());
    }

    @Deprecated
    public int getParentNeededLevel(SkillTreeNode parent, ParentType parentType) {
        for (var edge : parents)
            if (edge.getParent().equals(parent) && edge.getType() == parentType) return edge.getLevel();
        return 0;
    }

    @Deprecated
    public boolean hasIcon(NodeState status) {
        return getIcon(status) != null;
    }

    @Deprecated
    @Nullable
    public IconOptions getIcon(NodeState status) {
        for (var shape : NodeShape.values()) {
            var found = DisplayMap.getIcon(new NodeDisplayInfo(shape, status), icons);
            if (found != null) return found;
        }
        return null;
    }

    //endregion
}
