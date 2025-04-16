package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.PreviousPageItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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

import java.util.List;
import java.util.UUID;

public class EditableGuildView extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableGuildView() {
        super("guild-view");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("member")) return new MemberItem(config);
        if (function.equalsIgnoreCase("next")) return new NextPageItem<>(config);
        if (function.equalsIgnoreCase("previous")) return new PreviousPageItem<>(config);
        if (function.equalsIgnoreCase("disband")) return new DisbandItem(config);
        if (function.equalsIgnoreCase("invite")) return new InviteItem(config);
        if (function.equalsIgnoreCase("leave")) return new LeaveItem(config);

        return null;
    }

    public class LeaveItem extends SimpleItem<GuildViewInventory> {
        public LeaveItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            inv.playerData.getGuild().removeMember(inv.playerData.getUniqueId());
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            inv.getPlayer().closeInventory();
        }
    }

    public class InviteItem extends SimpleItem<GuildViewInventory> {
        public InviteItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(@NotNull GuildViewInventory inv) {
            return inv.isGuildOwner;
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            if (!inv.isGuildOwner) return;

            /*
             * if (playerData.getGuild().getMembers().count() >= max) {
             * ConfigMessage.fromKey("guild-is-full").send(inv.playerData);
             * inv.getPlayer().playSound(inv.getPlayer().getLocation(),
             * Sound.ENTITY_VILLAGER_NO, 1, 1); return; }
             */

            new ChatInput(inv.getPlayer(), PlayerInput.InputType.GUILD_INVITE, inv, input -> {
                Player target = Bukkit.getPlayer(input);
                if (target == null) {
                    ConfigMessage.fromKey("not-online-player", "player", input).send(inv.playerData);
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    inv.open();
                    return;
                }

                long remaining = inv.playerData.getGuild().getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                if (remaining > 0) {
                    ConfigMessage.fromKey("guild-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(inv.playerData);
                    inv.open();
                    return;
                }

                PlayerData targetData = PlayerData.get(target);
                if (inv.playerData.getGuild().hasMember(targetData.getUniqueId())) {
                    ConfigMessage.fromKey("already-in-guild", "player", target.getName()).send(inv.playerData);
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    inv.open();
                    return;
                }

                inv.playerData.getGuild().sendGuildInvite(inv.playerData, targetData);
                ConfigMessage.fromKey("sent-guild-invite", "player", target.getName()).send(inv.playerData);
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                inv.open();
            });
        }
    }

    public class DisbandItem extends SimpleItem<GuildViewInventory> {
        public DisbandItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            if (!inv.isGuildOwner) return;

            MMOCore.plugin.nativeGuildManager.unregisterGuild(inv.playerData.getGuild());
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            inv.getPlayer().closeInventory();
        }

        @Override
        public boolean isDisplayed(@NotNull GuildViewInventory inv) {
            return inv.isGuildOwner;
        }
    }

    public static class MemberDisplayItem extends PhysicalItem<GuildViewInventory> {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(GuildViewInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.members.get(n));
        }

        @Override
        public @NotNull Placeholders getPlaceholders(GuildViewInventory inv, int n) {
            UUID uuid = inv.members.get(n);
            OfflinePlayer player = getEffectivePlayer(inv, n);

            Placeholders holders = new Placeholders();
            OfflinePlayerData offline = OfflinePlayerData.get(uuid);
            holders.register("name", MMOCoreUtils.isInvalid(player) ? "???" : player.getName());
            holders.register("class", offline.getProfess().getName());
            holders.register("level", offline.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - offline.getLastLogin()));
            return holders;
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            UUID uuid = inv.members.get(n);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, uuid.toString());

            if (meta instanceof SkullMeta)
                inv.asyncUpdate(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            if (!inv.isGuildOwner) return;
            if (event.getClick() != ClickType.RIGHT) return;

            String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
            if (tag == null || tag.isEmpty())
                return;

            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(tag));
            if (target.equals(inv.getPlayer())) return;

            inv.playerData.getGuild().removeMember(target.getUniqueId());
            ConfigMessage.fromKey("kick-from-guild", "player", target.getName()).send(inv.playerData);
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    public class MemberItem extends SimpleItem<GuildViewInventory> {
        private final InventoryItem<GuildViewInventory> empty;
        private final MemberDisplayItem member;

        public MemberItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("empty"), "Could not load empty config");
            Validate.notNull(config.contains("member"), "Could not load member config");

            empty = new SimpleItem<>(config.getConfigurationSection("empty"));
            member = new MemberDisplayItem(this, config.getConfigurationSection("member"));
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            int index = n + inv.page * getSlots().size();
            return inv.playerData.getGuild().countMembers() > index ? member.getDisplayedItem(inv, index) : empty.getDisplayedItem(inv, index);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new GuildViewInventory(data);
    }

    public class GuildViewInventory extends GeneratedInventory {
        private final int maxpages;
        private final PlayerData playerData;
        private final boolean isGuildOwner;

        private List<UUID> members;

        public GuildViewInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditableGuildView.this);

            maxpages = (playerData.getGuild().countMembers() + 20) / getEditable().getByFunction("member").getSlots().size();
            this.playerData = playerData;
            isGuildOwner = playerData.getGuild().getOwner().equals(playerData.getUniqueId());
        }

        @Override
        public void open() {
            members = playerData.getGuild().listMembers();
            super.open();
        }

        @Override
        public @NotNull String getRawName() {
            return guiName
                    .replace("{online_players}", String.valueOf(playerData.getGuild().countOnlineMembers()))
                    .replace("{page}", "" + page).replace("{maxpages}", "" + maxpages)
                    .replace("{players}", String.valueOf(playerData.getGuild().countMembers()))
                    .replace("{tag}", playerData.getGuild().getTag())
                    .replace("{name}", playerData.getGuild().getName());
        }
    }
}
