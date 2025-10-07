package net.Indyuce.mmocore.loot.chest.particle;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.util.SchedulerAdapter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.BiConsumer;

public enum ChestParticleEffect {

	HELIX((loc, particle) -> {
		final double[] ti = {0};
		final BukkitTask[] task = new BukkitTask[1];
		task[0] = SchedulerAdapter.runTaskTimer(MMOCore.plugin, () -> {
			if ((ti[0] += Math.PI / 16) > Math.PI * 2)
				if (task[0] != null) task[0].cancel();
			for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
				loc.getWorld().spawnParticle(particle, loc.clone().add(Math.cos(j + ti[0] / 2), -.5 + ti[0] / Math.PI / 2, Math.sin(j + ti[0] / 2)), 0);
		}, 0, 1);
	}),

	OFFSET((loc, particle) -> {
		final int[] ti = {0};
		final BukkitTask[] task = new BukkitTask[1];
		task[0] = SchedulerAdapter.runTaskTimer(MMOCore.plugin, () -> {
			if (ti[0]++ > 20)
				if (task[0] != null) task[0].cancel();
			for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
				loc.getWorld().spawnParticle(particle, loc.clone(), 1, .5, .5, .5, 0);
		}, 0, 1);
	}),

	GALAXY((loc, particle) -> {
		final double[] ti = {0};
		final BukkitTask[] task = new BukkitTask[1];
		task[0] = SchedulerAdapter.runTaskTimer(MMOCore.plugin, () -> {
			if ((ti[0] += Math.PI / 16) > Math.PI * 2)
				if (task[0] != null) task[0].cancel();
			for (double j = 0; j < Math.PI * 2; j += Math.PI * 2 / 5)
				loc.getWorld().spawnParticle(particle, loc.clone().add(0, -.1, 0), 0, Math.cos(j + ti[0] / 2), 0, Math.sin(j + ti[0] / 2), .13);
		}, 0, 1);
	});

	private final BiConsumer<Location, Particle> func;

	ChestParticleEffect(BiConsumer<Location, Particle> func) {
		this.func = func;
	}

	public void play(Location loc, Particle particle) {
		func.accept(loc, particle);
	}
}
