package net.Indyuce.mmocore.gui.social.party;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EditablePartyView extends EditableInventory {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditablePartyView() {
        super("party-view");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("member")) return new MemberItem(config);
        if (function.equalsIgnoreCase("leave")) return new LeaveButton(config);
        if (function.equalsIgnoreCase("invite")) return new InviteItem(config);

        return null;
    }

    public class InviteItem extends SimpleItem<PartyViewInventory> {
        public InviteItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull PartyViewInventory inv, @NotNull InventoryClickEvent event) {
            Party party = (Party) inv.playerData.getParty();
            if (party.getMembers().size() >= MMOCore.plugin.configManager.maxPartyPlayers) {
                Message.PARTY_IS_FULL.send(inv.playerData);
                return;
            }

            new ChatInput(inv.getPlayer(), PlayerInput.InputType.PARTY_INVITE, inv, input -> {
                Player target = Bukkit.getPlayer(input);
                if (target == null) {
                    Message.PARTY_NOT_ONLINE_PLAYER.send(inv.playerData, "player", input);
                    return;
                }

                long remaining = party.getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                if (remaining > 0) {
                    Message.PARTY_INVITE_COOLDOWN.send(inv.playerData, "player", target.getName(), "cooldown", new DelayFormat().format(remaining));
                    return;
                }

                PlayerData targetData = PlayerData.get(target);
                if (party.hasMember(target)) {
                    Message.PARTY_ALREADY_IN.send(inv.playerData, "player", target.getName());
                    return;
                }

                int levelDifference = Math.abs(targetData.getLevel() - party.getLevel());
                if (levelDifference > MMOCore.plugin.configManager.maxPartyLevelDifference) {
                    Message.PARTY_HIGH_LEVEL_DIFFERENCE.send(inv.playerData, "player", target.getName(), "diff", String.valueOf(levelDifference));
                    return;
                }

                party.sendInvite(inv.playerData, targetData);
                Message.PARTY_SEND_INVITE.send(inv.playerData, "player", target.getName());
                inv.open();
            });
        }
    }

    public class LeaveButton extends SimpleItem<PartyViewInventory> {
        public LeaveButton(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull PartyViewInventory inv, @NotNull InventoryClickEvent event) {
            Party party = (Party) inv.playerData.getParty();
            party.removeMember(inv.playerData);
            Message.PARTY_LEAVE.send(inv.playerData); // Notify
            inv.getPlayer().closeInventory();
        }
    }

    public static class MemberDisplayItem extends PhysicalItem<PartyViewInventory> {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(PartyViewInventory inv, int n) {
            Party party = (Party) inv.playerData.getParty();
            PlayerData member = party.getMembers().get(n);

            Placeholders holders = new Placeholders();
            if (member.isOnline())
                holders.register("name", member.getPlayer().getName());
            holders.register("class", member.getProfess().getName());
            holders.register("level", member.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
            return holders;
        }

        @NotNull
        @Override
        public Player getEffectivePlayer(PartyViewInventory inv, int n) {
            return ((Party) inv.playerData.getParty()).getMembers().get(n).getPlayer();
        }

        @Override
        public ItemStack getDisplayedItem(PartyViewInventory inv, int n) {
            final Player member = getEffectivePlayer(inv, n);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, member.getUniqueId().toString());

            if (meta instanceof SkullMeta)
                inv.asyncUpdate(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(member);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public static class MemberItem extends InventoryItem<PartyViewInventory> {
        private final InventoryItem<PartyViewInventory> empty;
        private final MemberDisplayItem member;

        public MemberItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("empty"), "Could not load empty config");
            Validate.notNull(config.contains("member"), "Could not load member config");

            empty = new SimpleItem<>(config.getConfigurationSection("empty"));
            member = new MemberDisplayItem(this, config.getConfigurationSection("member"));
        }

        @Override
        public ItemStack getDisplayedItem(PartyViewInventory inv, int n) {
            Party party = (Party) inv.playerData.getParty();
            return party.getMembers().size() > n ? member.getDisplayedItem(inv, n) : empty.getDisplayedItem(inv, n);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void onClick(@NotNull PartyViewInventory inv, @NotNull InventoryClickEvent event) {
            Party party = (Party) inv.playerData.getParty();
            if (!party.getOwner().equals(inv.playerData)) return;

            final String uuidTag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
            if (uuidTag == null || uuidTag.isEmpty()) return;
            final OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuidTag));
            if (target.equals(inv.getPlayer())) return;

            var targetData = PlayerData.get(target);
            party.removeMember(targetData);
            Message.PARTY_KICKED_FROM.send(targetData);
            Message.PARTY_KICK_PLAYER.send(inv.playerData, "player", target.getName());
        }
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new PartyViewInventory(data);
    }

    public class PartyViewInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public PartyViewInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditablePartyView.this);

            this.playerData = playerData;
        }

        @Override
        public @NotNull String getRawName() {
            Party party = (Party) playerData.getParty();
            return guiName.replace("{max}", String.valueOf(MMOCore.plugin.configManager.maxPartyPlayers)).replace("{players}", String.valueOf(party.getMembers().size()));
        }
    }
}
