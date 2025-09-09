package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.player.Message;

public class PartyInvite extends Request {
    private final Party party;

    public PartyInvite(Party party, PlayerData creator, PlayerData target) {
        super(creator, target);

        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    @Override
    public void whenDenied() {
        // Nothing
    }

    @Override
    public void whenAccepted() {

        // Is party full?
        if (party.getMembers().size() >= MMOCore.plugin.configManager.maxPartyPlayers) {
            Message.PARTY_IS_FULL.send(getTarget());
            return;
        }

        // Remove invite
        if (getCreator().isOnline()) party.removeLastInvite(getCreator().getPlayer());

        // Notify other members of new member
        party.getMembers().forEach(member -> {
            if (member.isOnline())
                Message.PARTY_JOINED_OTHER.send(member, "player", getTarget().getPlayer().getName());
        });

        // Notify target
        // TODO replace after profile switch update. MythicLib now has a function to get the last player name
        Message.PARTY_JOINED.send(getTarget(), "owner", party.getOwner().lastKnownName);

        party.addMember(getTarget()); // Only add after to avoid double messages
        InventoryManager.PARTY_VIEW.newInventory(getTarget()).open();
    }
}