package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger;
import net.Indyuce.mmocore.api.quest.trigger.StaminaTrigger;
import net.Indyuce.mmocore.api.quest.trigger.StelliumTrigger;
import net.Indyuce.mmocore.command.builtin.mmocore.admin.ResourceCommandTreeNode;
import net.Indyuce.mmocore.skill.list.Neptune_Gift;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerResourceUpdateEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Type of resource being regenerated, this way
     * this event handles all four resources.
     */
    private final PlayerResource resource;
    private final UpdateReason reason;
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
    public PlayerResourceUpdateEvent(@NotNull PlayerData playerData, @NotNull PlayerResource resource, double oldAmount, double newAmount, @NotNull UpdateReason reason) {
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

    public double getNewAmount() {
        return newAmount;
    }

    public double getOriginalNewAmount() {
        return originalNewAmount;
    }

    public double getOldAmount() {
        return oldAmount;
    }

    @NotNull
    public UpdateReason getReason() {
        return reason;
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

    public enum UpdateReason {

        /**
         * When resource is being regenerated
         */
        REGENERATION,

        /**
         * When some resource is gained, or consumed by some skills
         */
        SKILL_REGENERATION,

        /**
         * When some resource is gained, or consumed by some skills
         */
        SKILL_COST,

        /**
         * When consuming stellium to use a waypoint
         */
        USE_WAYPOINT,

        /**
         * When the player chooses a class and mana from their previous
         * game session is restored
         */
        CHOOSE_CLASS,

        /**
         * Used by quests triggers
         * - {@link ManaTrigger}
         * - {@link StaminaTrigger}
         * - {@link StelliumTrigger}
         */
        TRIGGER,

        /**
         * When a player's "Max Resource" stat decreases so that the player's
         * current resource value needs to be brought down to avoid exceeding the
         * new max resource value, the player's current resource value gets updated
         * using this reason.
         */
        CLAMPING,

        /**
         * When using the resource command {@link ResourceCommandTreeNode}
         */
        COMMAND,

        /**
         * Reason not provided by user
         */
        UNKNOWN,

        /**
         * Anything else
         */
        OTHER;

        public boolean isRegeneration() {
            return this == REGENERATION || this == SKILL_REGENERATION;
        }

        public boolean isSkill() {
            return this == SKILL_COST || this == SKILL_REGENERATION;
        }
    }
}
