package net.Indyuce.mmocore.comp.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.OfflinePlayer;


public class MMOCorePlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Indyuce";
    }

    @Override
    public String getIdentifier() {
        return "mmocore";
    }

    @Override
    public String getVersion() {
        return MMOCore.plugin.getDescription().getVersion();
    }

    private static final String NO_PLAYER_PLACEHOLDER = "OfflinePlayer";

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        try {
            final var playerData = PlayerData.get(player);
            return PlaceholderEnum.parse(playerData, identifier);
        } catch (Exception exception) {
            return NO_PLAYER_PLACEHOLDER;
        }
    }
}
