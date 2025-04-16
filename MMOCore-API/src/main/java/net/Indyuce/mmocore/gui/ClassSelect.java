package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
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

public class ClassSelect extends EditableInventory {
    public ClassSelect() {
        super("class-select");
    }

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.startsWith("class")) return new ClassItem(config);
        return null;
    }

    public ProfessSelectionInventory newInventory(PlayerData data) {
        return newInventory(data, null);
    }

    public ProfessSelectionInventory newInventory(PlayerData data, @Nullable Runnable profileRunnable) {
        return new ProfessSelectionInventory(data, profileRunnable);
    }

    public class ClassItem extends SimpleItem<ProfessSelectionInventory> {
        private final PlayerClass playerClass;

        public ClassItem(ConfigurationSection config) {
            super(config);

            Validate.isTrue(config.getString("function").length() > 6, "Couldn't find the class associated to: " + config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(6));
            this.playerClass = MMOCore.plugin.classManager.getOrThrow(classId);
        }

        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(ProfessSelectionInventory inv, int n) {
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
        public void onClick(@NotNull ProfessSelectionInventory inv, @NotNull InventoryClickEvent event) {

            if (inv.profileRunnable == null && inv.playerData.getClassPoints() < 1) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("cant-choose-new-class").send(inv.playerData);
                return;
            }

            if (playerClass.hasOption(ClassOption.NEEDS_PERMISSION) && !inv.getPlayer().hasPermission("mmocore.class." + playerClass.getId().toLowerCase())) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("no-permission-for-class").send(inv.playerData);
                return;
            }

            if (playerClass.equals(inv.playerData.getProfess())) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("already-on-class", "class", playerClass.getName()).send(inv.getPlayer());
                return;
            }

            inv.getNavigator().unblockClosing();
            final PlayerClass playerClass = findDeepestSubclass(inv.playerData, this.playerClass);
            InventoryManager.CLASS_CONFIRM.get(MMOCoreUtils.ymlName(playerClass.getId())).newInventory(inv.getNavigator(), inv.playerData, inv.profileRunnable != null, inv.profileRunnable).open();
        }
    }

    public class ProfessSelectionInventory extends GeneratedInventory {
        @Nullable
        private final Runnable profileRunnable;
        private final PlayerData playerData;

        public ProfessSelectionInventory(PlayerData playerData, @Nullable Runnable profileRunnable) {
            super(new Navigator(playerData.getMMOPlayerData()), ClassSelect.this);

            this.playerData = playerData;
            this.profileRunnable = profileRunnable;
        }

        @Override
        public void open() {
            if (profileRunnable != null) getNavigator().blockClosing();
            super.open();
        }
    }

    /**
     * When switching from a class where you had progress before,
     * you should be instantly redirected to the highest subclass
     * in the subclass tree that you chose, because your progress
     * is saved there.
     * <p>
     * It's also more RPG style to take the player back to the subclass
     * he chose because that way he can't turn back and chose another path.
     * <p>
     * This does NOT function properly with subclass nets yet.
     *
     * @param root The root class, it's called the root because since the
     *             player was able to choose it in the class GUI, it should
     *             be at the bottom of the class tree.
     */
    private PlayerClass findDeepestSubclass(PlayerData player, PlayerClass root) {
        for (String checkedName : player.getSavedClasses()) {
            PlayerClass checked = MMOCore.plugin.classManager.getOrThrow(checkedName);
            if (root.hasSubclass(checked))
                return checked;
        }

        return root;
    }
}
