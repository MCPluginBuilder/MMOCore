package net.Indyuce.mmocore.api.event;

import io.lumine.mythic.lib.player.resource.AbstractHealthUpdateEvent;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.skill.list.Neptune_Gift;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerResourceUpdateEvent extends PlayerDataEvent implements AbstractHealthUpdateEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Type of resource being regenerated, this way
     * this event handles all four resources.
     */
    private final PlayerResource resource;
    private final ResourceUpdateReason reason;
    private final double oldAmount, originalNewAmount;

    /**
     * New amount. To obtain the amount of resource regenerated, if REASON
     * is REGENERATION, you must subtract oldAmount from newAmount.
     */
    private double newAmount;

    private boolean cancelled = false;

    /**
     * Called when a player gains some resource back. This can
     * be used to handle stats like health or mana regeneration.
     * <p>
     * Example use: {@link Neptune_Gift} which is a skill
     * that temporarily increases resource regeneration for a short amount of time.
     *
     * @param playerData Player regenerating
     * @param resource   Resource being increased
     * @param oldAmount  The old amount of resource before this event was called. Not modifiable.
     * @param newAmount  The new amount of resource after this event was called. Modifiable.
     * @param reason     The reason why this event was called
     */
    public PlayerResourceUpdateEvent(@NotNull PlayerData playerData, @NotNull PlayerResource resource, double oldAmount, double newAmount, @NotNull ResourceUpdateReason reason) {
        super(playerData);

        this.resource = resource;
        this.oldAmount = oldAmount;
        this.originalNewAmount = newAmount;
        this.newAmount = newAmount;
        this.reason = reason;
    }

    @NotNull
    public PlayerResource getResource() {
        return resource;
    }

    @Deprecated
    public double getAmount() {
        return getDifference();
    }

    public double getDifference() {
        return newAmount - oldAmount;
    }

    @Override
    public double getNewAmount() {
        return newAmount;
    }

    public double getOriginalNewAmount() {
        return originalNewAmount;
    }

    @Override
    public double getOldAmount() {
        return oldAmount;
    }

    @NotNull
    @Override
    public ResourceUpdateReason getUpdateReason() {
        return reason;
    }

    /**
     * @see ResourceUpdateReason
     * @see #getUpdateReason()
     * @deprecated
     */
    @Deprecated
    public UpdateReason getReason() {
        return UpdateReason.adapt(this.reason);
    }

    /**
     * @see #setNewAmount(double)
     * @deprecated
     */
    @Deprecated
    public void setAmount(double amount) {
        this.setNewAmount(amount + oldAmount);
    }

    /**
     * Sets the new amount of resource after the update.
     * Will not be applied if the event is cancelled.
     *
     * @param newAmount New amount of resource
     */
    public void setNewAmount(double newAmount) {
        this.newAmount = newAmount;
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
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Deprecated
    public enum UpdateReason {
        REGENERATION,
        SKILL_REGENERATION,
        SKILL_COST,
        USE_WAYPOINT,
        CHOOSE_CLASS,
        TRIGGER,
        CLAMPING,
        COMMAND,
        UNKNOWN,
        OTHER;

        @Deprecated
        public boolean isRegeneration() {
            return this == REGENERATION || this == SKILL_REGENERATION;
        }

        @Deprecated
        public boolean isSkill() {
            return this == SKILL_COST || this == SKILL_REGENERATION;
        }

        @Deprecated
        public ResourceUpdateReason adapt() {
            switch (this) {
                case REGENERATION:
                    return ResourceUpdateReason.REGENERATION;
                case COMMAND:
                    return ResourceUpdateReason.COMMAND;
                case TRIGGER:
                    return ResourceUpdateReason.MECHANIC;
                case OTHER:
                case UNKNOWN:
                    return ResourceUpdateReason.OTHER;
                case CHOOSE_CLASS:
                    return ResourceUpdateReason.CHOOSE_CLASS;
                case CLAMPING:
                    return ResourceUpdateReason.CLAMPING;
                case SKILL_COST:
                case SKILL_REGENERATION:
                    return ResourceUpdateReason.SKILL;
                case USE_WAYPOINT:
                    return ResourceUpdateReason.WAYPOINT;
            }

            // Fallback
            return ResourceUpdateReason.OTHER;
        }

        @Deprecated
        public static UpdateReason adapt(ResourceUpdateReason reason) {
            switch (reason) {
                case OTHER:
                    return OTHER;
                case REGENERATION:
                    return REGENERATION;
                case COMMAND:
                    return COMMAND;
                case CHOOSE_CLASS:
                    return CHOOSE_CLASS;
                case MECHANIC:
                    return TRIGGER;
                case CLAMPING:
                    return CLAMPING;
                case WAYPOINT:
                    return USE_WAYPOINT;
                case SKILL:
                    return SKILL_REGENERATION;
            }

            // Fallback
            return OTHER;
        }
    }
}
