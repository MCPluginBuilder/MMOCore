package net.Indyuce.mmocore.api.event;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

// TODO move to MythicLib
public class MMOCommandEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final String command;

    @Deprecated
    public MMOCommandEvent(PlayerData player, String command) {
        super(player);

        this.command = command;
    }

    public MMOCommandEvent(PlayerData player, CommandTreeRoot command) {
        super(player);

        this.command = command.getId();
    }

    public String getCommand() {
        return command;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
