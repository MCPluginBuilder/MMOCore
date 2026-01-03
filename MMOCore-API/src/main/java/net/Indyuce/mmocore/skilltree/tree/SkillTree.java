package net.Indyuce.mmocore.skilltree.tree;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.util.IconOptions;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.registry.RegisteredObject;
import net.Indyuce.mmocore.skilltree.*;
import net.Indyuce.mmocore.skilltree.display.DisplayMap;
import net.Indyuce.mmocore.skilltree.display.NodeShape;
import net.Indyuce.mmocore.skilltree.display.PathState;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * A passive skill tree that features nodes, or passive skills.
 * <p>
 * The player can explore the passive skill tree using the right GUI
 * and unlock nodes by spending passive skill points. Unlocking nodes
 * grant permanent player modifiers, including
 * - stats
 * - active or passive MythicLib skills
 * - active or passive MMOCore skills
 * - extra attribute pts
 * - particle or potion effects
 *
 * @see SkillTreeNode
 */
public abstract class SkillTree implements RegisteredObject {
    private final String id, name;
    private final List<String> lore = new ArrayList<>();
    private final Material item;
    private final int customModelData;
    protected final Map<String, SkillTreeNode> nodes = new HashMap<>();
    protected final int maxPointSpent;
    protected final List<SkillTreeNode> roots = new ArrayList<>();
    protected final DisplayMap icons;

    public SkillTree(@NotNull ConfigurationSection config) {
        this.id = Objects.requireNonNull(config.getString("id"), "Could not find skill tree id");
        this.name = MythicLib.plugin.parseColors(Objects.requireNonNull(config.getString("name"), "Could not find skill tree name"));
        Objects.requireNonNull(config.getStringList("lore"), "Could not find skill tree lore").forEach(str -> lore.add(MythicLib.plugin.parseColors(str)));
        this.item = Material.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("item"), "Could not find item")));
        this.customModelData = config.getInt("custom-model-data", 0);
        Validate.isTrue(config.isConfigurationSection("nodes"), "Could not find any nodes in the tree");
        this.maxPointSpent = config.getInt("max-point-spent", Integer.MAX_VALUE);

        // Load nodes
        for (String key : config.getConfigurationSection("nodes").getKeys(false))
            try {
                ConfigurationSection section = config.getConfigurationSection("nodes." + key);
                SkillTreeNode node = new SkillTreeNode(this, section);
                nodes.put(node.getId(), node);
                nodeByCoordinate.put(node.getCoordinates(), node);

                if (node.isRoot()) roots.add(node);
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Couldn't load skill tree node " + id + "." + key + ": " + exception.getMessage());
            }

        // Post load all nodes (relatives)
        for (var node : nodes.values())
            try {
                node.getPostLoadAction().performAction();
                node.getParents().forEach(parentInfo -> parentInfo.getElements().forEach(coords -> this.pathByCoordinate.put(coords, parentInfo)));
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Couldn't post-load skill tree node " + id + "." + node.getId() + ": " + exception.getMessage());
                exception.printStackTrace(); // TODO remove
            }

        // [Syntax Deprecated] Load legacy "paths" section
        loadLegacyPaths(config);

        // Resolve node shapes
        resolveNodeShapes();

        // Load icons
        this.icons = DisplayMap.from(config.getConfigurationSection("display"));
    }

    public List<String> getLore() {
        return lore;
    }

    public int getMaxPointSpent() {
        return maxPointSpent;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void addRoot(@NotNull SkillTreeNode node) {
        roots.add(node);
    }

    @NotNull
    public List<SkillTreeNode> getRoots() {
        return roots;
    }

    //region Resolving states and shapes

    private final Map<SkillTreeNode, NodeShape> nodeShapes = new HashMap<>();

    @NotNull
    public NodeShape getNodeShape(@NotNull SkillTreeNode node) {
        return Objects.requireNonNull(nodeShapes.get(node), "Missing node shape");
    }

    private void resolveNodeShapes() {
        for (var node : nodes.values()) nodeShapes.put(node, resolveNodeShape(node));
    }

    /**
     * Resets all states saved for the nodes and edges of
     * this skill tree and recomputes them from scratch solely
     * based on the nodes unlocked (node level map) by the
     * player and points spent in each of them.
     * <p>
     * TODO Use some collection and progressively filter out the nodes to avoid useless iterations
     * <p>
     * Let:
     * - A the number of path elements (not edges)
     * - V denote the number of nodes in the skill tree
     * - P the number of parents any node has, at most
     * - C the number of children any node has, at most
     * <p>
     * This algorithm runs in O(V * P * C). In Minecraft nodes
     * can't have more than 4 children or parents which makes
     * it O(V).
     *
     * @author jules
     */
    public void resolveStates(@NotNull PlayerData playerData) {
        playerData.clearStates(this);
        resolveNodeStates(playerData);
        resolvePathStates(playerData);
    }

    private void resolveNodeStates(@NotNull PlayerData playerData) {

        // If the player has already spent the maximum amount of points in this skill tree.
        final boolean skillTreeLocked = playerData.getPointsSpent(this) >= this.maxPointSpent;
        final NodeState lockState = skillTreeLocked ? NodeState.FULLY_LOCKED : NodeState.LOCKED;

        // PASS 1
        //
        // Initialization. Mark all nodes either locked or unlocked
        // Mark nodes as "Maxed out" if maximum level is reached.
        for (var node : nodes.values()) {
            final var nodeLevel = playerData.getNodeLevel(node);
            playerData.setNodeState(node, nodeLevel == 0 ? lockState : nodeLevel == node.getMaxLevel() ? NodeState.MAXED_OUT : NodeState.UNLOCKED);
        }

        if (skillTreeLocked) return;

        // PASS 2
        //
        // Apply level 1-unreachability rules in O(V * [C + P])
        // It has to differ from pass 1 because it uses results from pass 1.
        final var unreachable = new Stack<SkillTreeNode>();

        for (var node : nodes.values()) {

            // INCOMPATIBILITY RULES
            //
            // Any node with an unlocked incompatible parent is made unreachable.
            for (var edge : node.getParents())
                if (edge.getType() == ParentType.INCOMPATIBLE && playerData.getNodeState(edge.getParent()).isUnlocked()) {
                    unreachable.add(node);
                    break;
                }

            // MAX CHILDREN RULE
            //
            // If a node has N total children and M <= N are already unlocked,
            // the remaining N - M are made unreachable.
            final int maxChildren = node.getMaxChildren();
            if (maxChildren > 0) {

                int unlocked = 0;
                final var locked = new ArrayList<SkillTreeNode>();

                for (var edge : node.getChildren())
                    switch (playerData.getNodeState(edge.getChild())) {
                        case LOCKED:
                            locked.add(edge.getChild());
                            break;
                        case MAXED_OUT:
                        case UNLOCKED:
                            unlocked++;
                            break;
                    }

                if (unlocked >= maxChildren) unreachable.addAll(locked);
            }
        }

        // PASS 3
        //
        // Propagate level 1-unreachability in O(V * C * P)
        // Unreachability is transitive, if one node is unreachable, all subsequent
        // child nodes are all unreachable.
        final var unreachableCheck = new HashSet<SkillTreeNode>();
        while (!unreachable.empty()) {
            final var node = unreachable.pop();

            unreachableCheck.add(node);
            playerData.setNodeState(node, NodeState.FULLY_LOCKED);
            for (var edge : node.getChildren()) // Propagate
                if (edge.getType() == ParentType.STRONG) // TODO improve.
                    if (!unreachableCheck.contains(edge.getChild()) && isUnreachable(edge.getChild(), playerData))
                        unreachable.push(edge.getChild());
        }

        // PASS 4
        //
        // Mark unlockable nodes, in O(V * P). This rule does not need propagation
        // because the distance between the set of all unlocked nodes and the set
        // of all unlockable nodes is at most 1 (unlockability is not "transitive")
        pass4:
        for (var node : nodes.values()) {
            if (playerData.getNodeState(node) != NodeState.LOCKED) continue;

            // ROOT NODES
            //
            // Roots are either unlockable or unlocked.
            if (node.isRoot()) {
                playerData.setNodeState(node, NodeState.UNLOCKABLE);
                continue;
            }

            // STRONG & SOFT PARENTS
            //
            // For nodes with no strong/soft parents, the rule is nulled.
            // All strong parents of any node must be unlocked for the node to be unlockable.
            // One soft parent of any node must be unlocked for the node to be unlockable.
            boolean soft = false, hasSoft = false;

            for (var edge : node.getParents()) {
                if (edge.getType() == ParentType.STRONG && playerData.getNodeLevel(edge.getParent()) < edge.getLevel())
                    continue pass4; // Keep the node locked
                else if (!soft && edge.getType() == ParentType.SOFT) {
                    hasSoft = true;
                    if (playerData.getNodeLevel(edge.getParent()) >= edge.getLevel())
                        soft = true; // Cannot continue, must check for other strong parents
                }
            }

            // At least one soft parent!
            if (!hasSoft || soft) playerData.setNodeState(node, NodeState.UNLOCKABLE);
        }
    }

    private void resolvePathStates(@NotNull PlayerData playerData) {

        // PASS 5
        //
        // Resolve path states. Iterate through parents of nodes (children would work too)
        // TODO merge this with steps 1 to 4 (my brain is fried atm)
        for (var node : nodeByCoordinate.values())
            for (var edge : node.getParents())
                playerData.setPathState(edge, resolvePathState(playerData, edge));
    }

    @NotNull
    private PathState resolvePathState(@NotNull PlayerData playerData, @NotNull ParentInformation edge) {

        final var from = playerData.getNodeState(edge.getParent());
        final var to = playerData.getNodeState(edge.getChild());
        final var symm = edge.isSymmetrical();

        // Gray out path if target is fully locked
        // If symmetrical, check again after permutation
        if (to == NodeState.FULLY_LOCKED || (symm && from == NodeState.FULLY_LOCKED))
            return PathState.FULLY_LOCKED;

        // Both are unlocked => path is taken, unlocked
        // Symmetric relation so 'symm' does not matter
        if (from.isUnlocked() && to.isUnlocked()) return PathState.UNLOCKED;

        // If source is unlocked and target unlockable => UNLOCKABLE
        // If symmetrical, check again after permutation
        if ((from.isUnlocked() && to == NodeState.UNLOCKABLE) || (symm && (to.isUnlocked() && from == NodeState.UNLOCKABLE)))
            return PathState.UNLOCKABLE;

        // Locked path by default
        return PathState.LOCKED;
    }

    @NotNull
    private NodeShape resolveNodeShape(@NotNull SkillTreeNode node) {
        final var coordinates = node.getCoordinates();

        final var upCoords = new IntCoords(coordinates.getX(), coordinates.getY() - 1);
        final var downCoords = new IntCoords(coordinates.getX(), coordinates.getY() + 1);
        final var rightCoords = new IntCoords(coordinates.getX() + 1, coordinates.getY());
        final var leftCoords = new IntCoords(coordinates.getX() - 1, coordinates.getY());

        final var up = this.nodeByCoordinate.containsKey(upCoords) || this.pathByCoordinate.containsKey(upCoords);
        final var down = this.nodeByCoordinate.containsKey(downCoords) || this.pathByCoordinate.containsKey(downCoords);
        final var right = this.nodeByCoordinate.containsKey(rightCoords) || this.pathByCoordinate.containsKey(rightCoords);
        final var left = this.nodeByCoordinate.containsKey(leftCoords) || this.pathByCoordinate.containsKey(leftCoords);

        if (up && right && down && left) return NodeShape.UP_RIGHT_DOWN_LEFT;
        else if (up && right && down) return NodeShape.UP_RIGHT_DOWN;
        else if (up && right && left) return NodeShape.UP_RIGHT_LEFT;
        else if (up && down && left) return NodeShape.UP_DOWN_LEFT;
        else if (down && right && left) return NodeShape.DOWN_RIGHT_LEFT;
        else if (up && right) return NodeShape.UP_RIGHT;
        else if (up && down) return NodeShape.UP_DOWN;
        else if (up && left) return NodeShape.UP_LEFT;
        else if (down && right) return NodeShape.DOWN_RIGHT;
        else if (down && left) return NodeShape.DOWN_LEFT;
        else if (right && left) return NodeShape.RIGHT_LEFT;
        else if (up) return NodeShape.UP;
        else if (down) return NodeShape.DOWN;
        else if (right) return NodeShape.RIGHT;
        else if (left) return NodeShape.LEFT;
        return NodeShape.NO_PATH;
    }

    //endregion

    private boolean isUnreachable(@NotNull SkillTreeNode node, @NotNull PlayerData playerData) {

        // UNREACHABILITY RULES
        //
        // If at least one strong parent is unreachable, the node is unreachable too.
        // If all soft parents are unreachable, the node is unreachable.
        // This rule is the logical opposite of the reachability rule.
        boolean soft = false, hasSoft = false;

        for (var edge : node.getParents()) {
            if (edge.getType() == ParentType.STRONG && playerData.getNodeState(edge.getParent()) == NodeState.FULLY_LOCKED)
                return true;
            else if (!soft && edge.getType() == ParentType.SOFT) {
                hasSoft = true;
                if (playerData.getNodeState(edge.getParent()) != NodeState.FULLY_LOCKED)
                    soft = true; // Cannot continue, must check for other strong parents
            }
        }

        return hasSoft && !soft;
    }

    @NotNull
    public Material getItem() {
        return item;
    }

    @Override
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Collection<SkillTreeNode> getNodes() {
        return nodes.values();
    }

    //region Geometry

    protected final Map<IntCoords, SkillTreeNode> nodeByCoordinate = new HashMap<>();
    protected final Map<IntCoords, ParentInformation> pathByCoordinate = new HashMap<>();

    @NotNull
    public SkillTreeNode getNode(@NotNull IntCoords coords) {
        return Objects.requireNonNull(nodeByCoordinate.get(coords), "Could not find node in tree '" + id + "' with coordinates '" + coords + "'");
    }

    @Nullable
    public SkillTreeNode getNodeOrNull(@NotNull IntCoords coords) {
        return nodeByCoordinate.get(coords);
    }

    @Nullable
    public ParentInformation getPath(@NotNull IntCoords coords) {
        return pathByCoordinate.get(coords);
    }

    //endregion

    @NotNull
    public SkillTreeNode getNode(@NotNull String name) {
        return Objects.requireNonNull(nodes.get(name), "Could not find node in tree '" + id + "' with name '" + name + "'");
    }

    @NotNull
    public DisplayMap getIcons() {
        return icons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillTree skillTree = (SkillTree) o;
        return id.equals(skillTree.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //region Deprecated

    @Deprecated
    private void loadLegacyPaths(ConfigurationSection config) {

        int warnings = 0;

        for (var node : nodes.values()) {
            final var section = config.getConfigurationSection("nodes." + node.getId() + ".paths");
            if (section == null) continue;
            if (warnings++ < 3) {
                // Warn max 3 times
                MMOCore.log("You are using deprecated syntax for skill tree node '" + id + "' of skill tree '"
                        + this.getId() + "'. You may update your config to use the 'parents' section instead of the 'paths' section. " +
                        "Please visit 'https://gitlab.com/phoenix-dvpmt/mmocore/-/wikis/Skill%20Trees' to read about this new syntax");
            }
            node.loadLegacyPathSection(section);
        }

        // Repopulate pathByCoordinate map
        if (warnings > 0)
            for (var node : nodes.values())
                node.getParents().forEach(parentInfo -> parentInfo.getElements().forEach(coords -> this.pathByCoordinate.put(coords, parentInfo)));
    }

    @Deprecated
    public boolean isNode(@NotNull IntCoords coordinates) {
        // TODO remove usage
        return nodeByCoordinate.containsKey(coordinates);
    }

    @Deprecated
    public boolean isPath(@NotNull IntCoords coordinates) {
        // TODO remove usage
        return pathByCoordinate.containsKey(coordinates);
    }

    @Deprecated
    public boolean isNode(String name) {
        return nodes.containsKey(name);
    }

    @Deprecated
    public boolean isPathOrNode(IntCoords coordinates) {
        return isNode(coordinates) || isPath(coordinates);
    }

    @Deprecated
    public static SkillTree loadSkillTree(ConfigurationSection config) {
        return MMOCore.plugin.skillTreeManager.loadSkillTree(config);
    }

    @Deprecated
    public boolean hasIcon(Object displayInfo) {
        return DisplayMap.getIcon(displayInfo, icons) != null;
    }

    @Deprecated
    public IconOptions getIcon(Object displayInfo) {
        return DisplayMap.getIcon(displayInfo, icons);
    }

    //endregion
}
