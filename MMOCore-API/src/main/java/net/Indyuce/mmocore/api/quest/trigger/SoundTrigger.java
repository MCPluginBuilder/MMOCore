package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.message.SoundReader;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Sound;

public class SoundTrigger extends Trigger {
	private final SoundReader sound;

	public SoundTrigger(MMOLineConfig config) {
		super(config);

		config.validate("sound");

		sound = new SoundReader(config);
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		sound.play(player.getPlayer());
	}
}
