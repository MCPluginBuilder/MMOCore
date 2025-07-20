package net.Indyuce.mmocore.skilltree.display;

import net.Indyuce.mmocore.skilltree.PathState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PathDisplayInfo {
    private final PathShape shape;
    private final PathState state;

    public PathDisplayInfo(@NotNull PathShape shape, @NotNull PathState status) {
        this.state = status;
        this.shape = shape;
    }

    @NotNull
    public PathState getStatus() {
        return state;
    }

    @NotNull
    public PathShape getShape() {
        return shape;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape, state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathDisplayInfo that = (PathDisplayInfo) o;
        return shape == that.shape && state == that.state;
    }

    @Override
    public String toString() {
        return "PathDisplayInfo{" + "type=" + shape + ", status=" + state + '}';
    }
}
