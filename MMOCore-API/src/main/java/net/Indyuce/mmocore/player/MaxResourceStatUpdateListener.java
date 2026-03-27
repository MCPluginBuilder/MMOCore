package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.handler.StatUpdateListener;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
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

        // Use final value (not total) otherwise Max Health causes a problem
        // Fixes a bug with items that provide NEGATIVE Max Health
        // Minecraft naturally clamps max health to at least 1
        final var maxResourceValue = statInstance.getFinal();

        // Clamp current resource value
        final var mmocoreData = PlayerData.get(statInstance.getMap().getPlayerData().getUniqueId());
        final var currentResourceValue = this.resource.getCurrent(mmocoreData);
        if (currentResourceValue > maxResourceValue)
            this.resource.setCurrent(mmocoreData, maxResourceValue, ResourceUpdateReason.CLAMPING);
    }
}
