package net.Indyuce.mmocore.gui.eco;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.gui.PluginInventory;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.player.Message;
import net.Indyuce.mmocore.util.SchedulerAdapter;
import net.Indyuce.mmocore.util.item.SimpleItemBuilder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DepositMenu extends PluginInventory {
    private ItemStack depositItem;
    private int deposit;

    private Inventory lastBukkitInventory;

    /**
     * Every time an item is clicked in the inventory, an inventory
     * update is scheduled. If nothing happens for the next 10 ticks
     * then the update is processed. If another item is clicked within
     * this delay the task is cancelled and scheduled for later
     */
    private BukkitRunnable updateRunnable;

    public DepositMenu(Player player) {
        super(player);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 27, "Deposit");
        updateDeposit(inv);
        return lastBukkitInventory = inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {

        if (event.getCurrentItem() != null && event.getCurrentItem().isSimilar(depositItem)) {
            event.setCancelled(true);

            updateDeposit(event.getInventory());
            if (deposit <= 0) return;

            EconomyResponse response = MMOCore.plugin.economy.getEconomy().depositPlayer(player, deposit);
            if (!response.transactionSuccess()) return;

            event.getInventory().clear();
            player.closeInventory();
            Message.DEPOSIT_SUCCESS.send(player, "worth", String.valueOf(deposit));
            return;
        }

        // Can only move around currency items
        var worth = NBTItem.get(event.getCurrentItem()).getInteger("RpgWorth");
        if (worth < 1) event.setCancelled(true);
        else scheduleUpdate(event.getInventory());
    }

    @Override
    public void onClose() {

        // Cancel runnable
        if (updateRunnable != null)
            updateRunnable.cancel();

        // Give all items back
        SmartGive smart = new SmartGive(player);
        for (int j = 0; j < 26; j++) {
            ItemStack item = lastBukkitInventory.getItem(j);
            if (item != null)
                smart.give(item);
        }
    }

    private void scheduleUpdate(Inventory inv) {
        if (updateRunnable != null)
            updateRunnable.cancel();

        updateRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                updateRunnable = null;
                updateDeposit(inv);
            }
        };
        SchedulerAdapter.runTaskLater(MMOCore.plugin, updateRunnable, 10);
    }

    private void updateDeposit(Inventory inv) {
        if (updateRunnable != null) {
            updateRunnable.cancel();
            updateRunnable = null;
        }

        deposit = MMOCoreUtils.getWorth(inv.getContents());
        inv.setItem(26, depositItem = new SimpleItemBuilder("DEPOSIT_ITEM").addPlaceholders("worth", String.valueOf(deposit)).build());
    }
}
