package net.Indyuce.mmocore.api.event.social;

import net.Indyuce.mmocore.api.event.PlayerDataEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.provided.Party;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PartyChatEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Party party;

    private boolean cancelled;
    private String rawMessage;

    public PartyChatEvent(Party party, PlayerData playerData, String rawMessage) {
        super(playerData);

        this.party = party;
        this.rawMessage = rawMessage;
    }

    public void setMessage(@Nullable String rawMessage) {
        this.rawMessage = Objects.requireNonNull(rawMessage, "Message cannot be null");
    }

    @Nullable
    public String getMessage() {
        return rawMessage;
    }

    @NotNull
    public Party getParty() {
        return party;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
