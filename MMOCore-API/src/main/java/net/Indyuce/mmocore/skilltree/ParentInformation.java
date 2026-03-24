package net.Indyuce.mmocore.skilltree;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.skilltree.display.PathShape;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Holds information about a parent/child node in a skill tree.
 * `relative` can hold either a parent or child node. There is
 * always one `parent` counterpart for every `child` instance of this class.
 * <p>
 * If we represent the skill tree by a graph where edges are skill tree
 * "nodes", then this class represents an edge in that graph.
 *
 * @author jules
 */
public class ParentInformation {
    private final SkillTreeNode child, parent;
    private final ParentType type;
    private final int minLevel;
    private final boolean reciprocal;

    private final Map<IntCoords, PathShape> elements = new HashMap<>();

    public ParentInformation(@NotNull SkillTreeNode child, @NotNull SkillTreeNode parent) {
        this(child, parent, ParentType.SOFT, false, 1);
    }

    public ParentInformation(@NotNull SkillTreeNode child,
                             @NotNull SkillTreeNode parent,
                             @NotNull ParentType type,
                             boolean reciprocal,
                             int minLevel) {
        this.child = child;
        this.parent = parent;
        this.type = type;
        this.reciprocal = reciprocal;
        this.minLevel = Math.max(1, minLevel);
    }

    public ParentInformation(@NotNull SkillTreeNode child,
                             @NotNull SkillTreeNode parent,
                             @NotNull ParentType type,
                             @NotNull List<String> pathListRaw) {
        this.child = child;
        this.parent = parent;
        this.type = type;
        this.reciprocal = false;
        this.minLevel = 1;

        // Read paths
        pathListRaw.forEach(string -> this.elements.put(IntCoords.from(string), null));

        // All paths are loaded => cache their shapes
        for (var element : this.elements.keySet()) {
            final var previousValue = this.elements.put(element, computePathShape(element));
            Validate.isTrue(previousValue == null, "Path shape already computed?");
        }
    }

    public ParentInformation(@NotNull SkillTreeNode child,
                             @NotNull SkillTreeNode parent,
                             @NotNull ParentType type,
                             @NotNull ConfigurationSection config) {
        this.child = child;
        this.parent = parent;
        this.type = type;
        this.reciprocal = false;
        this.minLevel = Math.max(1, config.getInt("level"));

        // Read paths
        if (config.contains("paths")) {
            final var pathListRaw = config.getStringList("paths");
            pathListRaw.forEach(string -> this.elements.put(IntCoords.from(string), null));
        }

        // All paths are loaded => cache their shapes
        for (var element : this.elements.keySet()) {
            final var previousValue = this.elements.put(element, computePathShape(element));
            Validate.isTrue(previousValue == null, "Path shape already computed?");
        }
    }

    @NotNull
    public PathShape getShape(@NotNull IntCoords coordinates) {
        final var shape = this.elements.get(coordinates);
        Validate.notNull(shape, "No path element at " + coordinates);
        return shape;
    }

    @Deprecated
    public void addElement(@NotNull IntCoords coordinates) {
        Validate.isTrue(!this.elements.containsKey(coordinates), "Path element already present at " + coordinates);
        this.elements.put(coordinates, null); // Place new
        this.elements.replaceAll((e, v) -> computePathShape(e)); // Update all
    }

    /**
     * Defines the method for computing the shape of a path element ie
     * whether it goes up, right, up-right, etc. based on the presence
     * of other path elements around it.
     *
     * @param coordinates Coordinates of the path element to compute the shape for
     * @return Shape of the path element
     */
    @NotNull
    private PathShape computePathShape(@NotNull IntCoords coordinates) {

        final var upCoords = new IntCoords(coordinates.getX(), coordinates.getY() - 1);
        final var downCoords = new IntCoords(coordinates.getX(), coordinates.getY() + 1);
        final var rightCoords = new IntCoords(coordinates.getX() + 1, coordinates.getY());
        final var leftCoords = new IntCoords(coordinates.getX() - 1, coordinates.getY());

        final var hasUp = this.elements.containsKey(upCoords) || upCoords.equals(parent.getCoordinates()) || upCoords.equals(child.getCoordinates());
        final var hasDown = this.elements.containsKey(downCoords) || downCoords.equals(parent.getCoordinates()) || downCoords.equals(child.getCoordinates());
        final var hasRight = this.elements.containsKey(rightCoords) || rightCoords.equals(parent.getCoordinates()) || rightCoords.equals(child.getCoordinates());
        final var hasLeft = this.elements.containsKey(leftCoords) || leftCoords.equals(parent.getCoordinates()) || leftCoords.equals(child.getCoordinates());

        if ((hasUp || hasDown) && !hasLeft && !hasRight) return PathShape.UP;
        else if ((hasRight || hasLeft) && !hasUp && !hasDown) return PathShape.RIGHT;
        else if (hasUp && hasRight) return PathShape.UP_RIGHT;
        else if (hasUp && hasLeft) return PathShape.UP_LEFT;
        else if (hasDown && hasRight) return PathShape.DOWN_RIGHT;
        else if (hasDown && hasLeft) return PathShape.DOWN_LEFT;

        return PathShape.DEFAULT;
    }

    public boolean isSymmetrical() {
        return reciprocal;
    }

    @NotNull
    public Set<IntCoords> getElements() {
        return elements.keySet();
    }

    @Override
    public String toString() {
        return "ParentInformation{" +
                "child=" + child +
                ", parent=" + parent +
                ", type=" + type +
                ", minLevel=" + minLevel +
                ", elements=" + elements +
                '}';
    }

    @NotNull
    public SkillTreeNode getParent() {
        return parent;
    }

    @NotNull
    public SkillTreeNode getChild() {
        return child;
    }

    @NotNull
    public ParentType getType() {
        return type;
    }

    /**
     * @return Minimum level of parent node required
     * for the child node to be reachable.
     */
    public int getLevel() {
        return minLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentInformation that = (ParentInformation) o;
        // Big Hypothesis = there are NO two edges with the same child, parent and type.
        return Objects.equals(child, that.child) && Objects.equals(parent, that.parent) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(child, parent, type);
    }

    @NotNull
    public static ParentInformation fromConfig(@NotNull SkillTreeNode child,
                                               @NotNull SkillTreeNode parent,
                                               @NotNull ParentType parentType,
                                               @NotNull Object configObject) {
        Validate.notNull(configObject, "Cannot load parent info from null object");

        // From simple int, no path.
        if (configObject instanceof Integer) {
            // TODO try to infer paths from 'paths' config for backwards compatibility
            return new ParentInformation(child, parent, parentType, false, (Integer) configObject);
        }

        // From list, path list, level hard-set to 1
        if (configObject instanceof List) {
            return new ParentInformation(child, parent, parentType, (List<String>) configObject);
        }

        // From config section
        if (configObject instanceof ConfigurationSection) {
            return new ParentInformation(child, parent, parentType, (ConfigurationSection) configObject);
        }

        throw new IllegalArgumentException("Cannot load parent from " + configObject.getClass().getName());
    }
}
