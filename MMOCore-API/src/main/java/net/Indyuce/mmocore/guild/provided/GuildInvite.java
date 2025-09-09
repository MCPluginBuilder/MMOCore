package net.Indyuce.mmocore.guild.provided;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;

public class GuildInvite extends Request {
    private final Guild guild;

    public GuildInvite(Guild guild, PlayerData creator, PlayerData target) {
        super(creator, target);

        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public void whenDenied() {
        // Nothing
    }

    @Override
    public void whenAccepted() {
        guild.removeLastInvite(getCreator().getPlayer());

        // Notify target
        Message.GUILD_JOINED.send(getTarget().getPlayer(), "owner", Bukkit.getPlayer(guild.getOwner()).getName());

        // Notify members
        guild.forEachMember(member -> {
            if (Bukkit.getPlayer(member) != null) {
                Message.GUILD_JOINED_OTHER.send(Bukkit.getPlayer(member), "player", getTarget().getPlayer().getName());
            }
        });

        guild.addMember(getTarget().getUniqueId()); // Only add after, avoid dupe message
        InventoryManager.GUILD_VIEW.newInventory(getTarget()).open();
    }
}