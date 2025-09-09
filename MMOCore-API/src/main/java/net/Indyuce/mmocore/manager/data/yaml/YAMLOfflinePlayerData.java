package net.Indyuce.mmocore.manager.data.yaml;

import io.lumine.mythic.lib.util.config.YamlFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class YAMLOfflinePlayerData implements OfflinePlayerData {
    private final UUID uuid;
    private final YamlFile config;

    /**
     * Supports offline player data operations like friend removals which can't
     * be handled when their player data is not loaded in the data map.
     */
    @Deprecated
    public YAMLOfflinePlayerData(UUID uuid) {
        this.uuid = uuid;
        config = new YamlFile(MMOCore.plugin, "userdata", uuid.toString());
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void removeFriend(UUID uuid) {
        List<String> friends = config.getContent().getStringList("friends");
        friends.remove(uuid.toString());
        config.getContent().set("friends", friends);
        config.save();
    }

    @Override
    public boolean hasFriend(UUID uuid) {
        return config.getContent().getStringList("friends").contains(uuid.toString());
    }

    @Override
    public PlayerClass getProfess() {
        return config.getContent().contains("class") ? MMOCore.plugin.classManager.get(config.getContent().getString("class")) : MMOCore.plugin.classManager.getDefaultClass();
    }

    @Override
    public int getLevel() {
        return config.getContent().getInt("level");
    }

    @Override
    public long getLastLogin() {
        return config.getContent().getLong("last-login");
    }
}
