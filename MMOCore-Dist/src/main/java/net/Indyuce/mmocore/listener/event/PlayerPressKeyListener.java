package net.Indyuce.mmocore.listener.event;

import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * This registers all the KeyPress events. All events are registered
 * with LOWEST priority so that if the wrapped event happens to be
 * cancelled because of a key press, it is canceled before any plugin
 * can deal with it.
 */
public class PlayerPressKeyListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerCrouchKey(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            var called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.CROUCH, event);
            Bukkit.getPluginManager().callEvent(called);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerClickKey(PlayerClickEvent event) {
        if (event.getHand() == EquipmentSlot.MAIN_HAND) {
            var playerData = PlayerData.get(event.getPlayer());
            var key = event.isLeftClick() ? PlayerKey.LEFT_CLICK : PlayerKey.RIGHT_CLICK;
            Bukkit.getPluginManager().callEvent(new PlayerKeyPressEvent(playerData, key, event));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerDropKey(PlayerDropItemEvent event) {
        var playerData = PlayerData.get(event.getPlayer());
        Bukkit.getPluginManager().callEvent(new PlayerKeyPressEvent(playerData, PlayerKey.DROP, event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void registerSwapHandsKey(PlayerSwapHandItemsEvent event) {
        var called = new PlayerKeyPressEvent(PlayerData.get(event.getPlayer()), PlayerKey.SWAP_HANDS, event);
        Bukkit.getPluginManager().callEvent(called);
    }
}
