package net.Indyuce.mmocore.api.player.profess.event.trigger;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

@Deprecated
public class BlockPlacedTrigger implements EventTriggerHandler {

    @Override
    public boolean handles(String event) {
        return event.startsWith("place-block");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(BlockPlaceEvent event) {
        PlayerData player;
        try {
            player = PlayerData.get(event.getPlayer());
        } catch (NullPointerException exception) {
            // Fixes https://gitlab.com/phoenix-dvpmt/mythiclib/-/work_items/362
            return; // Player data not found
        }
        if (player.getProfess().hasEventTriggers("place-block"))
            player.getProfess().getEventTriggers("place-block").getTriggers().forEach(trigger -> trigger.apply(player));
    }
}
