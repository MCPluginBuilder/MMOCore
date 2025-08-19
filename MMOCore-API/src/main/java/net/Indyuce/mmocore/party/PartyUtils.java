package net.Indyuce.mmocore.party;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PartyUtils {

    @Deprecated
    public static void applyStatBonuses(PlayerData playerData, int memberCount) {
        updateStatBonuses(playerData, memberCount);
    }

    /**
     * Applies party stat bonuses to a specific player
     */
    public static void updateStatBonuses(@NotNull PlayerData player, int memberCount) {
        if (memberCount <= 1) clearStatBonuses(player);
        else for (var buff : MMOCore.plugin.partyManager.getBonuses())
            buff.multiply(memberCount - 1).register(player.getMMOPlayerData());
    }

    /**
     * Clear party stat bonuses from a player
     */
    public static void clearStatBonuses(@NotNull PlayerData player) {
        var playerData = player.getMMOPlayerData();
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
        if (MMOPlayerData.has(playerUuid)) {
            var playerData = MMOPlayerData.get(playerUuid);
            MMOCore.plugin.partyManager.getBonuses().forEach(buff -> buff.unregister(playerData));
        }
    }
}
