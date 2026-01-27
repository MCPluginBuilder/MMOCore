package net.Indyuce.mmocore.api.player.profess.resource;

import io.lumine.mythic.lib.util.TriConsumer;
import io.lumine.mythic.lib.version.Attributes;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger;
import net.Indyuce.mmocore.command.builtin.mmocore.admin.ResourceCommandTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum PlayerResource {

    HEALTH(data -> data.getPlayer().getHealth(),
            data -> data.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue(),
            PlayerData::heal,
            (data, amount, reason) -> data.getPlayer().setHealth(amount)),

    MANA(PlayerData::getMana,
            data -> data.getStats().getStat("MAX_MANA"),
            PlayerData::giveMana,
            PlayerData::setMana),

    STAMINA(PlayerData::getStamina,
            data -> data.getStats().getStat("MAX_STAMINA"),
            PlayerData::giveStamina,
            PlayerData::setStamina),

    STELLIUM(PlayerData::getStellium,
            data -> data.getStats().getStat("MAX_STELLIUM"),
            PlayerData::giveStellium,
            PlayerData::setStellium);

    private final String regenStat, maxRegenStat, maxStat;
    private final ClassOption offCombatRegen;
    private final Function<PlayerData, Double> current, max;

    // Used for MMOCore commands
    private final TriConsumer<PlayerData, Double, PlayerResourceUpdateEvent.UpdateReason> set, take, give;

    PlayerResource(@NotNull Function<PlayerData, Double> current,
                   @NotNull Function<PlayerData, Double> max,
                   @NotNull TriConsumer<PlayerData, Double, PlayerResourceUpdateEvent.UpdateReason> give,
                   @NotNull TriConsumer<PlayerData, Double, PlayerResourceUpdateEvent.UpdateReason> set) {
        this.regenStat = name() + "_REGENERATION";
        this.maxRegenStat = "MAX_" + name() + "_REGENERATION";
        this.maxStat = "MAX_" + name();
        this.offCombatRegen = ClassOption.valueOf("OFF_COMBAT_" + name() + "_REGEN");
        this.current = current;
        this.max = max;
        this.give = give;
        this.set = set;
        this.take = (data, amount, reason) -> this.give.accept(data, -amount, reason);
    }

    /**
     * @return Stat which corresponds to flat resource regeneration
     */
    public String getRegenStat() {
        return regenStat;
    }

    /**
     * @return Stat which corresponds to resource regeneration scaling with the player's max health
     */
    public String getMaxStat() {
        return maxStat;
    }

    /**
     * @return Stat which corresponds to resource regeneration scaling with the player's max health
     */
    public String getMaxRegenStat() {
        return maxRegenStat;
    }

    /**
     * @return Class option which determines whether or not resource should be
     *         regenerated off combat only
     */
    public ClassOption getOffCombatRegen() {
        return offCombatRegen;
    }

    /**
     * @return Current resource of the given player
     */
    public double getCurrent(@NotNull PlayerData player) {
        return current.apply(player);
    }

    /**
     * @return Max amount of that resource of the given player
     */
    public double getMax(@NotNull PlayerData player) {
        return max.apply(player);
    }

    /**
     * Regens a player resource. Whatever resource, a bukkit event is triggered
     *
     * @param player Player to regen
     * @param amount Amount to regen
     */
    public void regen(@NotNull PlayerData player, double amount) {
        this.give.accept(player, amount, PlayerResourceUpdateEvent.UpdateReason.REGENERATION);
    }

    /**
     * Sets a player resource. Whatever resource, a bukkit event is triggered
     *
     * @param player Player to regen
     * @param amount Amount to set
     * @param reason Reason for the update
     */
    public void setCurrent(@NotNull PlayerData player, double amount, @NotNull PlayerResourceUpdateEvent.UpdateReason reason) {
        this.set.accept(player, amount, reason);
    }

    /**
     * Used by MMOCore admin commands here: {@link ResourceCommandTreeNode}
     */
    public TriConsumer<PlayerData, Double, PlayerResourceUpdateEvent.UpdateReason> getConsumer(ManaTrigger.Operation operation) {
        switch (operation) {
            case SET:
                return set;
            case TAKE:
                return take;
            case GIVE:
                return give;
            default:
                throw new IllegalArgumentException("Operation not supported");
        }
    }
}
