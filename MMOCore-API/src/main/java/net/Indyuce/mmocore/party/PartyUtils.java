package net.Indyuce.mmocore.party;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PartyUtils {

    @Deprecated
    public static void updateStatBonuses(PlayerData playerData, int ignored) {
        resolvePartyBonuses(playerData);
    }

    public static void resolvePartyBonuses(@NotNull PlayerData playerData) {
        final var party = MMOCore.plugin.partyModule.getParty(playerData);
        if (party == null) clearStatBonuses(playerData);
        else applyStatBonuses(playerData, party.countMembers());
    }

    public static void applyStatBonuses(@NotNull UUID playerId, int memberCount) {
        final var playerData = MMOPlayerData.getOrNull(playerId);
        if (playerData != null) applyStatBonuses(playerData, memberCount);
    }

    public static void applyStatBonuses(@NotNull PlayerData playerData, int memberCount) {
        applyStatBonuses(playerData.getMMOPlayerData(), memberCount);
    }

    public static void applyStatBonuses(@NotNull MMOPlayerData playerData, int memberCount) {
        if (memberCount < 2) clearStatBonuses(playerData);
        else for (var buff : MMOCore.plugin.partyManager.getBonuses())
            buff.multiply(memberCount - 1).register(playerData);
    }

    public static void clearStatBonuses(@NotNull PlayerData playerData) {
        clearStatBonuses(playerData.getMMOPlayerData());
    }

    public static void clearStatBonuses(@NotNull MMOPlayerData playerData) {
        MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(playerData));
    }

    /**
     * Used to clear stat bonuses for a player that might have already
     * logged out the server. Some party plugins, including Parties,
     * remove the player from the party after a set delay (in case the
     * player relogs within 10 minutes for instance), in which case
     * the PlayerData instance is no longer available.
     * <p>
     * The MMOPlayerData is not guaranteed to be available, since it
     * is only flushed after 1 hour of inactivity.
     * <p>
     * Fixes MMOCore#1121
     *
     * @param playerUuid UUID of player supposedly offline
     */
    public static void clearStatBonuses(@NotNull UUID playerUuid) {
        final var playerData = MMOPlayerData.getOrNull(playerUuid);
        if (playerData != null) clearStatBonuses(playerData);
    }
}
