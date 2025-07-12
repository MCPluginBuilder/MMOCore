package net.Indyuce.mmocore.gui.social.friend;

import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.GoBackItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditableFriendRemoval extends EditableInventory {
    public EditableFriendRemoval() {
        super("friend-removal");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("yes")) return new YesItem(config);
        if (function.equalsIgnoreCase("back")) return new GoBackItem<ClassConfirmationInventory>(config) {

            @Override
            public @NotNull Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
                Placeholders inherited = super.getPlaceholders(inv, n);
                inherited.register("name", inv.friend.getName());
                return inherited;
            }
        };

        return null;
    }

    public class YesItem extends SimpleItem<ClassConfirmationInventory> {
        public YesItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public @NotNull Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("name", inv.friend.getName());
            return holders;
        }

        @Override
        public void onClick(@NotNull ClassConfirmationInventory inv, @NotNull InventoryClickEvent event) {
            inv.playerData.removeFriend(inv.friend.getUniqueId());
            OfflinePlayerData.get(inv.friend.getUniqueId()).removeFriend(inv.playerData.getUniqueId());
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            ConfigMessage.fromKey("no-longer-friends", "unfriend", inv.friend.getName()).send(inv.playerData);
            inv.getNavigator().popOpen();
        }
    }

    public ClassConfirmationInventory newInventory(EditableFriendList.FriendListInventory inventory, OfflinePlayer friend) {
        return new ClassConfirmationInventory(inventory, friend);
    }

    public class ClassConfirmationInventory extends GeneratedInventory {
        private final OfflinePlayer friend;
        private final PlayerData playerData;

        public ClassConfirmationInventory(EditableFriendList.FriendListInventory inventory, OfflinePlayer friend) {
            super(inventory.getNavigator(), EditableFriendRemoval.this);

            this.playerData = inventory.getPlayerData();
            this.friend = friend;
        }
    }
}
