package net.Indyuce.mmocore.skilltree.display;

import net.Indyuce.mmocore.skilltree.NodeState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NodeDisplayInfo {
    private final NodeShape shape;
    private final NodeState state;

    public NodeDisplayInfo(@NotNull NodeShape shape, @NotNull NodeState status) {
        this.state = status;
        this.shape = shape;
    }

    @NotNull
    public NodeState getStatus() {
        return state;
    }

    @NotNull
    public NodeShape getShape() {
        return shape;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDisplayInfo that = (NodeDisplayInfo) o;
        return state == that.state && shape == that.shape;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, shape);
    }

    @Override
    public String toString() {
        return "NodeDisplayInfo{" + "status=" + state + ", type=" + shape + '}';
    }
}
