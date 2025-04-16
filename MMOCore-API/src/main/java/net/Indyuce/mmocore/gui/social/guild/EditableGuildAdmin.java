package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Deprecated
public class EditableGuildAdmin extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableGuildAdmin() {
        super("guild-admin");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("member")) return new MemberItem(config);
        if (function.equalsIgnoreCase("leave")) return new LeaveItem(config);
        if (function.equalsIgnoreCase("invite")) return new InviteItem(config);
        return null;
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new GuildViewInventory(data);
    }

    public class InviteItem extends SimpleItem<GuildViewInventory> {
        public InviteItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            if (inv.playerData.getGuild().countMembers() >= inv.max) {
                ConfigMessage.fromKey("guild-is-full").send(inv.playerData);
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

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
                    ConfigMessage.fromKey("guild-invite-cooldown", "player", target.getName(), "cooldown",
                            new DelayFormat().format(remaining)).send(inv.playerData);
                    inv.open();
                    return;
                }

                PlayerData targetData = PlayerData.get(target);
                if (inv.playerData.getGuild().hasMember(target.getUniqueId())) {
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

    public static class MemberDisplayItem extends PhysicalItem<GuildViewInventory> {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GuildViewInventory inv, int n) {
            PlayerData member = PlayerData.get(inv.members.get(n));

            Placeholders holders = new Placeholders();

            if (member.isOnline())
                holders.register("name", member.getPlayer().getName());
            holders.register("class", member.getProfess().getName());
            holders.register("level", "" + member.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
            return holders;
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            UUID uuid = inv.members.get(n);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, uuid.toString());

            if (meta instanceof SkullMeta && offlinePlayer != null)
                inv.asyncUpdate(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(offlinePlayer);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public static class MemberItem extends InventoryItem<GuildViewInventory> {
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
            return inv.playerData.getGuild().countMembers() > n ? member.getDisplayedItem(inv, n) : empty.getDisplayedItem(inv, n);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void onClick(@NotNull GuildViewInventory inv, @NotNull InventoryClickEvent event) {
            if (!inv.isGuildOwner) return;

            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING)));
            if (target.equals(inv.getPlayer())) return;

            inv.playerData.getGuild().removeMember(target.getUniqueId());
            ConfigMessage.fromKey("kick-from-guild", "player", target.getName()).send(inv.playerData);
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    public class GuildViewInventory extends GeneratedInventory {
        private final int max;
        private final PlayerData playerData;
        private final boolean isGuildOwner;

        private List<UUID> members;

        public GuildViewInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditableGuildAdmin.this);

            max = getEditable().getByFunction("member").getSlots().size();
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
            return guiName.replace("{max}", String.valueOf(max)).replace("{players}", String.valueOf(playerData.getGuild().countMembers()));
        }
    }
}
