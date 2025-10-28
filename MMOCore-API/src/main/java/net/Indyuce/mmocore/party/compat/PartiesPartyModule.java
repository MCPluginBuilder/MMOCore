package net.Indyuce.mmocore.party.compat;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import io.lumine.mythic.lib.util.Tasks;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import net.Indyuce.mmocore.party.PartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PartiesPartyModule implements PartyModule, Listener {

    public PartiesPartyModule() {
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        final var api = Parties.getApi();
        final var partyPlayer = api.getPartyPlayer(playerData.getUniqueId());
        final var partyId = partyPlayer.getPartyId();
        if (partyId == null) return null;

        Party party = api.getParty(partyId);
        return party == null ? null : new PartyImpl(party);
    }

    @EventHandler
    public void onPartyCreate(BukkitPartiesPartyPostCreateEvent event) {

        // Should be one but you never know
        final var memberCount = event.getParty().getMembers().size();

        // !! async event !!
        Tasks.runSync(MMOCore.plugin, () -> {

            // Apply stats to online members
            applyToMembers(event.getParty(), memberCount);
        });
    }

    @EventHandler
    public void onPartyDelete(BukkitPartiesPartyPostDeleteEvent event) {

        // !! async event !!
        Tasks.runSync(MMOCore.plugin, () -> {

            // Clear bonuses from online members
            event.getParty().getOnlineMembers().forEach(member -> PartyUtils.clearStatBonuses(member.getPlayerUUID()));
        });
    }

    @EventHandler
    public void onPlayerJoin(BukkitPartiesPlayerPostJoinEvent event) {
        final var newMemberCount = event.getParty().getMembers().size();

        // !!! async event !!!
        Tasks.runSync(MMOCore.plugin, () -> {

            // Apply stats to online members, including new member
            applyToMembers(event.getParty(), newMemberCount);
        });
    }

    @EventHandler
    public void onPlayerLeave(BukkitPartiesPlayerPostLeaveEvent event) {
        final var newMemberCount = event.getParty().getMembers().size();

        // !!! async event !!!
        Tasks.runSync(MMOCore.plugin, () -> {

            // Try to clear stat bonuses from leaving player
            // Might be offline when leaving
            PartyUtils.clearStatBonuses(event.getPartyPlayer().getPlayerUUID());

            // Update stats for online members
            applyToMembers(event.getParty(), newMemberCount);
        });
    }

    private void applyToMembers(Party party, int memberCount) {
        party.getOnlineMembers().forEach(member -> PartyUtils.applyStatBonuses(member.getPlayerUUID(), memberCount));
    }

    static class PartyImpl implements AbstractParty {
        private final Party party;

        public PartyImpl(Party party) {
            this.party = party;
        }

        @Override
        public boolean hasMember(@NotNull Player player) {
            for (PartyPlayer member : party.getOnlineMembers())
                if (member.getPlayerUUID().equals(player.getUniqueId())) return true;

            return false;
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            final var list = new ArrayList<PlayerData>(party.getOnlineMembers().size());

            for (PartyPlayer member : party.getOnlineMembers())
                list.add(PlayerData.get(member.getPlayerUUID()));

            return list;
        }

        @Override
        public int countMembers() {
            return party.getMembers().size();
        }
    }
}
