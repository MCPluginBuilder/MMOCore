package net.Indyuce.mmocore.util;


import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @param <L> Left-hand side type
 * @param <R> Right-hand side type
 * @see io.lumine.mythic.lib.util.Pair
 * @deprecated
 */
@Deprecated
public class Pair<L, R> {
    private final L left;
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @NotNull
    public String toString() {
        return "(" + this.getLeft() + ',' + this.getRight() + ')';
    }

    @NotNull
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) object;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
