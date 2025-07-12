package net.Indyuce.mmocore.gui.eco;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.gui.PluginInventory;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GoldPouch extends PluginInventory {
    private final boolean mob;
    private final NBTItem nbt;

    private Inventory lastBukkitInventory;

    public GoldPouch(Player player, NBTItem nbt) {
        super(player);

        this.nbt = nbt;
        this.mob = nbt.getBoolean("RpgPouchMob");
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 18, ChatColor.UNDERLINE + "Gold Pouch");
        inv.setContents(MMOCoreUtils.itemStackArrayFromBase64(nbt.getString("RpgPouchInventory")));
        return lastBukkitInventory = inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        NBTItem nbt = NBTItem.get(item);
        if (!nbt.hasTag("RpgWorth")) {
            event.setCancelled(true);
            return;
        }

        if (mob) {
            event.setCancelled(true);

            // in deposit menu
            if (event.getSlot() < 18) {
                int empty = player.getInventory().firstEmpty();
                if (empty < 0)
                    return;

                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1, 2);
                player.getInventory().addItem(event.getCurrentItem());
                event.getInventory().setItem(event.getSlot(), null);
            }

            return;
        }

        /*
         * Player cannot interact with a backpack item while
         * interacting with a backpack inventory. This fixes a
         * huge glitch where the player would lose the backpack
         * contents
         */
        if (nbt.hasTag("RpgPouchInventory"))
            event.setCancelled(true);
    }

    @Override
    public void onClose() {
        if (mob && isEmpty(lastBukkitInventory)) {
            player.getEquipment().setItemInMainHand(null);
            return;
        }

        ItemStack updated = NBTItem.get(player.getEquipment().getItemInMainHand()).addTag(new ItemTag("RpgPouchInventory", MMOCoreUtils.toBase64(lastBukkitInventory.getContents()))).toItem();
        player.getEquipment().setItemInMainHand(updated);
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents())
            if (item != null && item.getType() != Material.AIR)
                return false;
        return true;
    }
}
