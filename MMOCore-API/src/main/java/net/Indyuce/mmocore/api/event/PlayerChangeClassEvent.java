package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerChangeClassEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerClass newClass;
    private final Reason reason;

    private boolean cancelled = false;

    @Deprecated
    public PlayerChangeClassEvent(PlayerData player, PlayerClass newClass) {
        this(player, newClass, Reason.UNKNOWN);
    }

    public PlayerChangeClassEvent(PlayerData player, PlayerClass newClass, Reason reason) {
        super(player);

        this.reason = reason;
        this.newClass = newClass;
    }

    @NotNull
    public Reason getReason() {
        return reason;
    }

    @NotNull
    public PlayerClass getNewClass() {
        return newClass;
    }

    public boolean isSubclass() {
        return getData().getProfess().getSubclasses().stream().anyMatch(sub -> sub.getProfess().equals(newClass));
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static enum Reason {

        /**
         * Class is selected (class switch) by an admin command.
         */
        COMMAND_SELECT,

        /**
         * Class is forcefully changed by an admin command.
         */
        COMMAND_FORCE,

        /**
         * When the player changes class using the class change GUI
         */
        GUI,

        /**
         * Not specified by user
         */
        UNKNOWN
    }
}
