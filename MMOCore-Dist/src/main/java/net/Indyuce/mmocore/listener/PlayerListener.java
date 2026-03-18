package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.session.SessionUpdateEvent;
import io.lumine.mythic.lib.profile.ProfileSessionState;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class PlayerListener implements Listener {

    /**
     * Updates the player's combat log data every time he hits an entity, or
     * gets hit by an entity or a projectile sent by another entity
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateCombat(PlayerAttackEvent event) {
        PlayerData.get(event.getAttacker().getPlayer()).getCombat().update();
    }

    /**
     * Updates the player's combat log everytime he gets hit.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateCombat(EntityDamageEvent event) {
        if (UtilityMethods.isFake(event)) return;
        if (UtilityMethods.isRealPlayer(event.getEntity()) && MMOCore.plugin.configManager.combatLogDamageCauses.contains(event.getCause()))
            PlayerData.get((Player) event.getEntity()).getCombat().update();
    }

    /**
     * Using the Bukkit health update event is not a good way of
     * interacting with MMOCore health regeneration. The
     * PlayerResourceUpdateEvent should be heavily prioritized.
     * <p>
     * This method makes sure that all the plugins which
     * utilize this event can also communicate with MMOCore
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void resourceBukkitInterface(PlayerResourceUpdateEvent event) {
        if (event.getResource() == PlayerResource.HEALTH) {
            final var bukkitEvent = new EntityRegainHealthEvent(event.getPlayer(), event.getDifference(), RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(bukkitEvent);

            // Update event values
            event.setNewAmount(event.getOldAmount() + bukkitEvent.getAmount());
            event.setCancelled(bukkitEvent.isCancelled());
        }
    }

    @SuppressWarnings("deprecation")
    @BackwardsCompatibility(version = "unspecified")
    @EventHandler
    public void backwardsCompatibilityEvent(PlayerLevelChangeEvent event) {
        if (event.getReason() == PlayerLevelChangeEvent.Reason.LEVEL_UP)
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(event.getData(), event.getProfession(), event.getOldLevel(), event.getNewLevel()));
    }

    /**
     * Runs on priority HIGH to make sure it executes after
     * MMOItems inventory, items and stats are resolved
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSessionUpdate(SessionUpdateEvent event) {
        // On session open, set resources again after all stats are properly loaded.
        if (event.getNewState() == ProfileSessionState.OPEN)
            PlayerData.get(event.getPlayerData().getPlayer()).onSessionOpen();
    }
}
