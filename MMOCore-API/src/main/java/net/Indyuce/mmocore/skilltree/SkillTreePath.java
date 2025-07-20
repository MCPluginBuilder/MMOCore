package net.Indyuce.mmocore.skilltree;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skilltree.display.PathShape;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;

public class SkillTreePath {
    private final SkillTree tree;
    private final IntegerCoordinates coordinates;
    private final SkillTreeNode from;
    private final SkillTreeNode to;

    public SkillTreePath(SkillTree tree, IntegerCoordinates coordinates, SkillTreeNode from, SkillTreeNode skillTreeNode) {
        this.tree = tree;
        this.coordinates = coordinates;
        this.from = from;
        to = skillTreeNode;
    }

    /**
     * Defines the status of a path between two nodes, which is determined
     * by the pair of states of the two nodes.
     */
    public PathState getStatus(PlayerData playerData) {
        var from = playerData.getNodeState(this.from);
        var to = playerData.getNodeState(this.to);

        // Either one is fully locked => gray out path
        if (from == NodeState.FULLY_LOCKED || to == NodeState.FULLY_LOCKED) return PathState.FULLY_LOCKED;

        // Both are unlocked => path is taken, unlocked
        if (from.isUnlocked() && to.isUnlocked()) return PathState.UNLOCKED;

        // One of them is unlocked, other one is unlockable => path is not taken yet, but can be
        if ((from == NodeState.UNLOCKABLE && to.isUnlocked()) || (from.isUnlocked() && to == NodeState.UNLOCKABLE))
            return PathState.UNLOCKABLE;

        // Otherwise, locked path
        return PathState.LOCKED;
    }

    public PathShape getPathType() {
        IntegerCoordinates upCoor = new IntegerCoordinates(coordinates.getX(), coordinates.getY() - 1);
        IntegerCoordinates downCoor = new IntegerCoordinates(coordinates.getX(), coordinates.getY() + 1);
        IntegerCoordinates rightCoor = new IntegerCoordinates(coordinates.getX() + 1, coordinates.getY());
        IntegerCoordinates leftCoor = new IntegerCoordinates(coordinates.getX() - 1, coordinates.getY());
        boolean hasUp = tree.isPath(upCoor) || upCoor.equals(from.getCoordinates()) || upCoor.equals(to.getCoordinates());
        boolean hasDown = tree.isPath(downCoor) || downCoor.equals(from.getCoordinates()) || downCoor.equals(to.getCoordinates());
        boolean hasRight = tree.isPath(rightCoor) || rightCoor.equals(from.getCoordinates()) || rightCoor.equals(to.getCoordinates());
        boolean hasLeft = tree.isPath(leftCoor) || leftCoor.equals(from.getCoordinates()) || leftCoor.equals(to.getCoordinates());

        if ((hasUp || hasDown) && !hasLeft && !hasRight) {
            return PathShape.UP;
        } else if ((hasRight || hasLeft) && !hasUp && !hasDown) {
            return PathShape.RIGHT;
        } else if (hasUp && hasRight) {
            return PathShape.UP_RIGHT;
        } else if (hasUp && hasLeft) {
            return PathShape.UP_LEFT;
        } else if (hasDown && hasRight) {
            return PathShape.DOWN_RIGHT;
        } else if (hasDown && hasLeft) {
            return PathShape.DOWN_LEFT;
        }
        return PathShape.DEFAULT;
    }
}
