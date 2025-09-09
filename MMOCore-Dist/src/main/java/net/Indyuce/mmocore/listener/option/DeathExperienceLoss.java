package net.Indyuce.mmocore.listener.option;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathExperienceLoss implements Listener {
    private final double loss = MMOCore.plugin.getConfig().getDouble("death-exp-loss.percent") / 100;

    /**
     * Note that some combat log plugins typically cause issues with this
     * event. They sometimes have players die AFTER they leave the server,
     * leading to a non-existent PlayerData object.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void a(PlayerDeathEvent event) {

        // Note that some combat log plugins may cause issues with this
        if (!PlayerData.has(event.getEntity())) return;

        var playerData = PlayerData.get(event.getEntity());
        var loss = (int) (playerData.getExperience() * this.loss);
        playerData.setExperience(playerData.getExperience() - loss);

        if (playerData.isOnline()) Message.DEATH_EXP_LOSS.send(playerData, "loss", loss);
    }
}
