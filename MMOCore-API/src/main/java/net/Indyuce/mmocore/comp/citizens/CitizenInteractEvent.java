package net.Indyuce.mmocore.comp.citizens;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CitizenInteractEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	private final NPC npc;

	public CitizenInteractEvent(Player who, NPC npc) {
		super(who);
		this.npc = npc;
	}

	public NPC getNPC() {
		return npc;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
