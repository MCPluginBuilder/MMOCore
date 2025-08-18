package net.Indyuce.mmocore.util;

import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.annotation.NotUsed;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * References to string-identified objects that are hashable
 * and can be passed as map keys. This is useful for objects
 * like attributes, skills... which all suffer the same problem:
 * <p>
 * When the plugin is reloaded, existing references to dead objects
 * should all be killed. A simple method to kill this problem is to
 * wrap all references in "LiveReferences" with a #reload() method
 * that releases the previous object ready to be garbage collected,
 * while keeping its previous (and unchanged) identifier so that
 * the live reference can grab the next, valid, version of the object
 * whenever it is needed.
 * <p>
 * This implementation is similar to {@link Lazy} objects. Since all
 * live references are persistent, there is no need to flush the object,
 * it is simpler to just reload the reference as soon as possible and
 * spare a null-check on runtime.
 *
 * @param <T> Type parameter
 */
@Deprecated
@NotUsed
public class LiveReference<T> {
    private final String identifier;
    private final Function<String, T> supplier;

    /**
     * Value must evaluate to a non-null value.
     */
    @NotNull
    private T value;

    /**
     * @param supplier     How object is supplied given its identifier. The supplier is expected
     *                     to throw a runtime exception in case the object cannot be identified anymore.
     * @param initialValue Initial value. Cannot be null
     * @param identifier   Object identifier
     */
    protected LiveReference(@NotNull Function<String, T> supplier, @NotNull T initialValue, @NotNull String identifier) {
        this.identifier = identifier;
        this.supplier = supplier;
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    @NotNull
    public String getId() {
        return this.identifier;
    }

    public void reload() {
        this.value = this.supplier.apply(this.identifier);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LiveReference<?> that = (LiveReference<?>) object;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    //region Static methods

    private static final Function<String, PlayerClass> PLAYER_CLASS_PROVIDER = id -> MMOCore.plugin.classManager.getOrThrow(id);
    private static final Function<String, PlayerAttribute> ATTRIBUTE_PROVIDER = id -> MMOCore.plugin.attributeManager.getOrThrow(id);

    @Nullable
    public static LiveReference<PlayerClass> playerClass(@Nullable PlayerClass playerClass) {
        if (playerClass == null) return null;
        return new LiveReference<>(PLAYER_CLASS_PROVIDER, playerClass, playerClass.getId());
    }

    @Nullable
    public static LiveReference<PlayerAttribute> playerAttribute(@Nullable PlayerAttribute playerAttribute) {
        if (playerAttribute == null) return null;
        return new LiveReference<>(ATTRIBUTE_PROVIDER, playerAttribute, playerAttribute.getId());
    }

    //endregion
}
