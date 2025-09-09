package net.Indyuce.mmocore.experience;

import net.Indyuce.mmocore.manager.social.BoosterManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Exp booster
 */
public class Booster {
    private final UUID uuid = UUID.randomUUID();
    private final long date = System.currentTimeMillis();
    private final Profession profession;
    private final double extra;
    private final String author;

    /**
     * Length is not final because boosters can stacks. This allows to reduce
     * the amount of boosters displayed in the main player menu
     * <p>
     * See {@link BoosterManager#register(Booster)}
     */
    private long duration;

    /**
     * @param extra    1 for +100% experience, 3 for 300% etc.
     * @param duration Booster duration in seconds
     */
    public Booster(double extra, long duration) {
        this(null, null, extra, duration);
    }

    /**
     * Main class experience booster
     *
     * @param author   The booster creator
     * @param extra    1 for +100% experience, 3 for 300% etc.
     * @param duration Booster length in seconds
     */
    public Booster(@Nullable String author, double extra, long duration) {
        this(author, null, extra, duration);
    }

    /**
     * Profession experience booster
     *
     * @param author     The booster creator
     * @param profession Either null for main level boosters or a specific profession
     * @param extra      1 for +100% experience, 3 for 300% etc.
     * @param duration   Booster length in seconds
     */
    public Booster(@Nullable String author, @Nullable Profession profession, double extra, long duration) {
        this.author = author;
        this.duration = duration * 1000;
        this.profession = profession;
        this.extra = extra;
    }

    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    public double getExtra() {
        return extra;
    }

    public boolean hasAuthor() {
        return author != null;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    public long getCreationDate() {
        return date;
    }

    public boolean hasProfession() {
        return profession != null;
    }

    @Nullable
    public Profession getProfession() {
        return profession;
    }

    public boolean isTimedOut() {
        return date + duration < System.currentTimeMillis();
    }

    public long getLeft() {
        return Math.max(0, date + duration - System.currentTimeMillis());
    }

    public long getDuration() {
        return duration;
    }

    public void addDuration(long duration) {
        this.duration += duration;
    }

    public boolean canStackWith(Booster booster) {
        return extra == booster.extra && Objects.equals(profession, booster.profession);
    }

    @Deprecated
    public long getLength() {
        return getDuration();
    }

    @Deprecated
    public void addLength(long length) {
        addDuration(length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booster booster = (Booster) o;
        return Objects.equals(uuid, booster.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
