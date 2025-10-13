package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * @see PlayerLevelChangeEvent
 * @see net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent.Reason#LEVEL_UP
 * @deprecated Level up can now be detected through PlayerLevelChangeEvent with a reason of LEVEL_UP.
 */
@Deprecated
public class PlayerLevelUpEvent extends PlayerDataEvent {
    private static final HandlerList handlers = new HandlerList();

    // If null, this is main level
    private final Profession profession;
    private final int oldLevel, newLevel;

    /**
     * @see PlayerLevelChangeEvent
     * @deprecated
     */
    @Deprecated
    public PlayerLevelUpEvent(PlayerData player, int oldLevel, int newLevel) {
        this(player, null, oldLevel, newLevel);
    }

    /**
     * @see PlayerLevelChangeEvent
     * @deprecated
     */
    @Deprecated
    public PlayerLevelUpEvent(PlayerData player, Profession profession, int oldLevel, int newLevel) {
        super(player);

        this.profession = profession;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public boolean hasProfession() {
        return profession != null;
    }

    @Nullable
    public Profession getProfession() {
        return profession;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
