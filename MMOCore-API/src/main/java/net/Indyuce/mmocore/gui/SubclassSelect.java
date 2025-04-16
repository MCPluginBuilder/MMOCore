package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.GoBackItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SubclassSelect extends EditableInventory {
    public SubclassSelect() {
        super("subclass-select");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.startsWith("sub-class")) return new ClassItem(config);
        if (function.equalsIgnoreCase("back")) return new GoBackItem<>(config);
        return null;
    }

    public GeneratedInventory newInventory(PlayerData data) {
        ClassSelect.ProfessSelectionInventory prev = InventoryManager.CLASS_SELECT.newInventory(data);
        return new SubclassSelectionInventory(prev.getNavigator(), data);
    }

    public class ClassItem extends SimpleItem<SubclassSelectionInventory> {
        private final PlayerClass playerClass;

        public ClassItem(ConfigurationSection config) {
            super(config);

            Validate.isTrue(config.getString("function").length() > 10, "Couldn't find the class associated to: " + config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(10));
            this.playerClass = MMOCore.plugin.classManager.getOrThrow(classId);
        }

        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(SubclassSelectionInventory inv, int n) {
            ItemOptions options = n == 0 ? ItemOptions.item(n, playerClass.getIcon()) : ItemOptions.index(n);
            ItemStack item = super.getDisplayedItem(inv, options);
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName())
                meta.setDisplayName(meta.getDisplayName().replace("{name}", playerClass.getName()));
            List<String> lore = meta.getLore();

            int index = lore.indexOf(ChatColor.GRAY + "{lore}");
            if (index >= 0) {
                lore.remove(index);
                for (int j = 0; j < playerClass.getDescription().size(); j++)
                    lore.add(index + j, playerClass.getDescription().get(j));
            }

            index = lore.indexOf(ChatColor.GRAY + "{attribute-lore}");
            if (index >= 0) {
                lore.remove(index);
                for (int j = 0; j < playerClass.getAttributeDescription().size(); j++)
                    lore.add(index + j, playerClass.getAttributeDescription().get(j));
            }

            meta.getPersistentDataContainer().set(new NamespacedKey(MMOCore.plugin, "class_id"), PersistentDataType.STRING, playerClass.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public boolean isDisplayed(@NotNull SubclassSelectionInventory inv) {
            return inv.playerData
                    .getProfess()
                    .getSubclasses()
                    .stream()
                    .anyMatch(subclass -> subclass.getLevel() <= inv.playerData.getLevel()
                            && subclass.getProfess().getId().equals(playerClass.getId()));
        }

        @Override
        public void onClick(@NotNull SubclassSelectionInventory inv, @NotNull InventoryClickEvent event) {

            if (inv.playerData.getClassPoints() < 1) {
                inv.getPlayer().closeInventory();
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("cant-choose-new-class").send(inv.playerData);
                return;
            }
            if (playerClass.hasOption(ClassOption.NEEDS_PERMISSION) && !inv.getPlayer().hasPermission("mmocore.class." + playerClass.getId().toLowerCase())) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("no-permission-for-class").send(inv.playerData);
                return;
            }

            InventoryManager.CLASS_CONFIRM.get(playerClass.getId()).newInventory(inv.getNavigator(), inv.playerData, true, null).open();
        }
    }

    public class SubclassSelectionInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public SubclassSelectionInventory(Navigator navigator, PlayerData playerData) {
            super(navigator, SubclassSelect.this);

            this.playerData = playerData;
        }
    }
}
