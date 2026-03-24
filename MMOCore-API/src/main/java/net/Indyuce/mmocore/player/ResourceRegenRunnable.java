package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ResourceRegenRunnable extends BukkitRunnable {

    private boolean scheduled = false;
    private double tickPeriodMultiplier;

    @Override
    public void run() {
        for (var player : PlayerData.getAll())
            // Check if the player is dead, otherwise
            // will cause glitches in the respawn menu
            if (player.getMMOPlayerData().isPlaying() && !player.getPlayer().isDead()) tickResourceRegen(player);
    }

    void tickResourceRegen(@NotNull PlayerData player) {
        for (var resource : PlayerResource.values()) tickResourceRegen(player, resource);
    }

    void tickResourceRegen(@NotNull PlayerData player, @NotNull PlayerResource resource) {
        final var regenAmount = player.getProfess().getHandler(resource).getRegen(player);
        if (regenAmount != 0) resource.regen(player, regenAmount * tickPeriodMultiplier);
    }

    public void schedule() {
        Validate.isTrue(!this.scheduled, "ResourceRegenRunnable is already scheduled");
        this.scheduled = true;

        final var tickPeriod = Math.max(1, MMOCore.plugin.getConfig().getLong("player_resource_tick_period"));
        this.tickPeriodMultiplier = (double) tickPeriod / 20d;
        this.runTaskTimer(MMOCore.plugin, tickPeriod, tickPeriod);
    }
}
