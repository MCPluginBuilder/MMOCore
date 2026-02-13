package net.Indyuce.mmocore.manager.social;

import net.Indyuce.mmocore.experience.Booster;
import net.Indyuce.mmocore.experience.Profession;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BoosterManager {
	private final List<Booster> active = new ArrayList<>();

	public void unregister(Booster booster) {
		active.remove(booster);
	}

	/**
	 * If MMOCore can find a booster with the same profession and value, the two
	 * boosters will stack to reduce the amount of boosters displayed at the
	 * same time. Otherwise, booster is registered
	 * 
	 * @param booster
	 *            Booster to register
	 */
	public void register(Booster booster) {

		// flushes booster list to reduce future calculations
		flush();

		for (Booster active : active)
			if (active.canStackWith(booster)) {
				active.addDuration(booster.getDuration());
				return;
			}

		active.add(booster);
	}

	public Booster get(int index) {
		flush();
		return active.get(index);
	}

	/**
	 * Cleans timed out boosters from the MMOCore registry
	 */
	private void flush() {
		active.removeIf(Booster::isTimedOut);
	}

	/**
	 * @return Sums all current experience boosters values
	 */
	public double getMultiplier(@Nullable Profession profession) {
		double d = 1;

		for (Booster booster : active)
			if (Objects.equals(profession, booster.getProfession()) && !booster.isTimedOut())
				d += booster.getExtra();

		return d;
	}

	/**
	 * @return Collection of currently registered boosters. Some of them can be
	 *         expired but are not unregistered yet!
	 */
	public List<Booster> getActive() {
		return active.stream().filter((b) -> !b.isTimedOut()).collect(Collectors.toList());
	}
}
