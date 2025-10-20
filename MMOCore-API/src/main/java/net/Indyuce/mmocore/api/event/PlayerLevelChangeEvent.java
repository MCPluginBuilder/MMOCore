package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLevelChangeEvent extends PlayerDataEvent {
    private final int oldLevel, newLevel;
    @Nullable(value = "null if class levels")
    private final Profession profession;
    private final Reason reason;

    private static final HandlerList HANDLERS = new HandlerList();

    @Deprecated
    public PlayerLevelChangeEvent(PlayerData player, int oldLevel, int newLevel) {
        this(player, null, oldLevel, newLevel, Reason.UNKNOWN);
    }

    @Deprecated
    public PlayerLevelChangeEvent(PlayerData player, Profession profession, int oldLevel, int newLevel) {
        this(player, profession, oldLevel, newLevel, Reason.UNKNOWN);
    }

    public PlayerLevelChangeEvent(PlayerData player, @Nullable Profession profession, int oldLevel, int newLevel, Reason reason) {
        super(player);

        this.profession = profession;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.reason = reason;
    }

    @NotNull
    public Reason getReason() {
        return reason;
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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static enum Reason {

        /**
         * Players level up one of their profession or main class
         */
        LEVEL_UP,

        /**
         * Command to change the player's level
         */
        COMMAND,

        /**
         * Their level is reset using the player data reset command
         */
        RESET,

        /**
         * When a player changes their current class
         */
        CHOOSE_CLASS,

        /**
         * When a player logs in or chooses their profile
         */
        CHOOSE_PROFILE,

        /**
         * Not provided by the user
         */
        UNKNOWN,

        /**
         * Not used internally
         */
        OTHER,
    }
}
