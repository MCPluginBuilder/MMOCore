package net.Indyuce.mmocore.api.event;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomPlayerFishEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Item droppedItem;

	private boolean cancelled = false;

	public CustomPlayerFishEvent(PlayerData player, Item droppedItem) {
		super(player);

		this.droppedItem = droppedItem;
	}

	public Item getDroppedItem() {
		return droppedItem;
	}

	public ItemStack getCaught() {
		return droppedItem.getItemStack();
	}

	public void setCaught(@NotNull ItemStack caught) {
		droppedItem.setItemStack(caught);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
