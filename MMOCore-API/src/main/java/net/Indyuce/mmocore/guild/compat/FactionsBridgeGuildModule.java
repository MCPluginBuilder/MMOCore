package net.Indyuce.mmocore.guild.compat;

import cc.javajobs.factionsbridge.FactionsBridge;
import cc.javajobs.factionsbridge.bridge.infrastructure.struct.FPlayer;
import cc.javajobs.factionsbridge.bridge.infrastructure.struct.Faction;
import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FactionsBridgeGuildModule implements GuildModule {

    @Override
    public AbstractGuild getGuild(PlayerData playerData) {
        final var api = FactionsBridge.getFactionsAPI();
        final var faction = api.getFaction(playerData.getPlayer());
        return faction == null ? null : new CustomGuild(faction);
    }

    @Override
    public @NotNull Relationship getRelationship(Player player, Player target) {
        final var api = FactionsBridge.getFactionsAPI();

        var faction = api.getFaction(player);
        if (faction != null) return adapt(faction.getRelationshipTo(api.getFPlayer(target)));

        faction = api.getFaction(target);
        if (faction != null) return adapt(faction.getRelationshipTo(api.getFPlayer(player)));

        return Relationship.GUILD_NEUTRAL;
    }

    private Relationship adapt(cc.javajobs.factionsbridge.bridge.infrastructure.struct.Relationship rel) {
        switch (rel) {
            case ENEMY:
                return Relationship.GUILD_ENEMY;
            case ALLY:
            case MEMBER:
                return Relationship.GUILD_ALLY;
            case NONE:
            case TRUCE:
            default:
                return Relationship.GUILD_NEUTRAL;
        }
    }

    static class CustomGuild implements AbstractGuild {

        @NotNull
        private final Faction faction;

        CustomGuild(Faction faction) {
            this.faction = Objects.requireNonNull(faction);
        }

        @Override
        public boolean hasMember(Player player) {
            for (FPlayer member : faction.getMembers())
                if (member.getUniqueId().equals(player.getUniqueId())) return true;
            return false;
        }
    }
}
