package net.Indyuce.mmocore.gui.social.friend;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.PreviousPageItem;
import io.lumine.mythic.lib.gui.editable.placeholder.ErrorPlaceholders;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.player.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EditableFriendList extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableFriendList() {
        super("friend-list");
    }

    @Override
    public @Nullable io.lumine.mythic.lib.gui.editable.item.InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equals("friend")) return new FriendItem(config);
        if (function.equals("previous")) return new PreviousPageItem<>(config);
        if (function.equals("next")) return new NextPageItem<>(config);
        if (function.equals("request")) return new RequestItem(config);

        return null;
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new FriendListInventory(data);
    }

    class OfflineFriendItem extends PhysicalItem<FriendListInventory> {
        public OfflineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(FriendListInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.playerData.getFriends().get(n));
        }

        @Override
        public @NotNull Placeholders getPlaceholders(FriendListInventory inv, int n) {
            OfflinePlayer friend = getEffectivePlayer(inv, n);
            if (MMOCoreUtils.isInvalid(friend)) return new ErrorPlaceholders();

            Placeholders holders = new Placeholders();
            holders.register("name", friend.getName());
            holders.register("last_seen", new DelayFormat(2).format(System.currentTimeMillis() - friend.getLastPlayed()));
            return holders;
        }
    }

    class OnlineFriendItem extends SimpleItem<FriendListInventory> {
        public OnlineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(FriendListInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.playerData.getFriends().get(n));
        }

        @Override
        public @NotNull Placeholders getPlaceholders(FriendListInventory inv, int n) {
            final PlayerData friendData = PlayerData.get(getEffectivePlayer(inv, n));

            Placeholders holders = new Placeholders();
            if (friendData.isOnline())
                holders.register("name", friendData.getPlayer().getName());
            holders.register("class", friendData.getProfess().getName());
            holders.register("level", friendData.getLevel());
            holders.register("online_since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));
            return holders;
        }
    }

    class RequestItem extends SimpleItem<FriendListInventory> {
        public RequestItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull FriendListInventory inv, @NotNull InventoryClickEvent event) {
            long remaining = inv.playerData.getActivityTimeOut(PlayerActivity.FRIEND_REQUEST);
            if (remaining > 0) {
                Message.FRIEND_REQUEST_COOLDOWN.send(inv.playerData, "cooldown", new DelayFormat().format(remaining));
                return;
            }

            new ChatInput(inv.getPlayer(), InputType.FRIEND_REQUEST, inv, input -> {
                Player target = Bukkit.getPlayer(input);
                if (target == null) {
                    Message.FRIEND_NOT_ONLINE_PLAYER.send(inv.playerData, "player", input);
                    inv.open();
                    return;
                }

                if (inv.playerData.hasFriend(target.getUniqueId())) {
                    Message.FRIEND_ALREADY.send(inv.playerData, "player", target.getName());
                    inv.open();
                    return;
                }

                if (inv.playerData.getUniqueId().equals(target.getUniqueId())) {
                    Message.FRIEND_CANT_FRIEND_YOURSELF.send(inv.playerData);
                    inv.open();
                    return;
                }

                inv.playerData.sendFriendRequest(PlayerData.get(target));
                Message.FRIEND_SENT_REQUEST.send(inv.playerData, "player", target.getName());
                inv.open();
            });

        }
    }

    class FriendItem extends SimpleItem<FriendListInventory> {
        private final OnlineFriendItem online;
        private final OfflineFriendItem offline;

        public FriendItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("online"), "Could not load online config");
            Validate.notNull(config.contains("offline"), "Could not load offline config");

            online = new OnlineFriendItem(this, config.getConfigurationSection("online"));
            offline = new OfflineFriendItem(this, config.getConfigurationSection("offline"));
        }

        @Override
        public ItemStack getDisplayedItem(FriendListInventory inv, int n) {
            if (inv.playerData.getFriends().size() <= n)
                return super.getDisplayedItem(inv, n);

            final OfflinePlayer friend = Bukkit.getOfflinePlayer(inv.playerData.getFriends().get(n));
            ItemStack disp = (friend.isOnline() ? online : offline).getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, friend.getUniqueId().toString());
            if (meta instanceof SkullMeta)
                inv.asyncUpdate(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(friend);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void onClick(@NotNull FriendListInventory inv, @NotNull InventoryClickEvent event) {
            if (event.getClick() != ClickType.RIGHT) return;

            String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
            if (tag == null || tag.isEmpty()) return;

            InventoryManager.FRIEND_REMOVAL.newInventory(inv, Bukkit.getOfflinePlayer(UUID.fromString(tag))).open();
        }
    }

    class FriendListInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public FriendListInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditableFriendList.this);

            this.playerData = playerData;
        }

        public PlayerData getPlayerData() {
            return playerData;
        }
    }
}
