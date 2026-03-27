package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.player.resource.HealthUpdateEventSupplier;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealthEventSupplier implements HealthUpdateEventSupplier<PlayerResourceUpdateEvent> {

    @Override
    public @NotNull PlayerResourceUpdateEvent onHealthUpdate(@NotNull Player player, double oldValue, double newValue, @NotNull ResourceUpdateReason reason) {
        return new PlayerResourceUpdateEvent(PlayerData.get(player), PlayerResource.HEALTH, oldValue, newValue, reason);
    }
}
