package net.Indyuce.mmocore.api.player.profess;

import io.lumine.mythic.lib.util.lang3.Validate;

public class Subclass {
	private final PlayerClass profess;
	private final int level;

	public Subclass(PlayerClass profess, int level) {
		Validate.notNull(profess, "Subclass cannot be null");

		this.profess = profess;
		this.level = level;
	}

	public PlayerClass getProfess() {
		return profess;
	}

	public int getLevel() {
		return level;
	}
}