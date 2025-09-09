package net.Indyuce.mmocore.guild.provided;

import io.lumine.mythic.lib.util.config.YamlFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YAMLGuildDataManager extends GuildDataManager {
	@Override
	public void save(Guild guild) {
		var config = getGuildDataFile(guild);
		config.getContent().set("name", guild.getName());
		config.getContent().set("tag", guild.getTag());
		config.getContent().set("owner", guild.getOwner().toString());

		List<String> memberList = new ArrayList<>();
		guild.forEachMember(uuid -> memberList.add(uuid.toString()));
		config.getContent().set("members", memberList);

		config.save();
	}

	@Override
	public void load() {
		File guildsFolder = new File(MMOCore.plugin.getDataFolder(), "guilds");
		if (!guildsFolder.exists())
			guildsFolder.mkdirs();
		for (File file : guildsFolder.listFiles()) {
			if (!file.isDirectory() && file.getName().substring(file.getName().lastIndexOf('.')).equalsIgnoreCase(".yml")) {
				FileConfiguration c = YamlConfiguration.loadConfiguration(file);
				Guild guild = newRegisteredGuild(UUID.fromString(c.getString("owner")), c.getString("name"), c.getString("tag"));
				for (String m : c.getStringList("members"))
					guild.registerMember(UUID.fromString(m));
			}
		}
	}

    private static YamlFile getGuildDataFile(Guild guild) {
        return new YamlFile(MMOCore.plugin, "guilds", guild.getId());
    }

	@Override
	public void delete(Guild guild) {
		getGuildDataFile(guild).delete();
	}
}
