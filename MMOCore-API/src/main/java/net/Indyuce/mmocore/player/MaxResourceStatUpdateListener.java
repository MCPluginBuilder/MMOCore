package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.handler.StatUpdateListener;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import org.jetbrains.annotations.NotNull;

public class MaxResourceStatUpdateListener implements StatUpdateListener {
    private final PlayerResource resource;

    public MaxResourceStatUpdateListener(PlayerResource resource) {
        this.resource = resource;
    }

    @Override
    public void onUpdate(@NotNull StatInstance statInstance) {
        final var maxResourceValue = statInstance.getTotal();

        // Clamp current resource value
        final var mmocoreData = PlayerData.get(statInstance.getMap().getPlayerData().getUniqueId());
        final var currentResourceValue = this.resource.getCurrent(mmocoreData);
        if (currentResourceValue > maxResourceValue)
            this.resource.setCurrent(mmocoreData, maxResourceValue, PlayerResourceUpdateEvent.UpdateReason.CLAMPING);
    }
}
