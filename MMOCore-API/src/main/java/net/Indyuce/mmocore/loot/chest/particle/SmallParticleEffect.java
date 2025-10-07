package net.Indyuce.mmocore.loot.chest.particle;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.util.SchedulerAdapter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

public class SmallParticleEffect implements Runnable {
	private final Location loc;
	private final Particle particle;
	private final double r;
	private BukkitTask task;

	private double t;

	public SmallParticleEffect(Entity entity, Particle particle) {
		this(entity, particle, .7);
	}

	public SmallParticleEffect(Entity entity, Particle particle, double r) {
		this.loc = entity.getLocation().add(0, .5, 0);
		this.particle = particle;
		this.r = r;

		task = SchedulerAdapter.runTaskTimer(MMOCore.plugin, this, 0, 1);
	}

	public void run() {
		if (t > Math.PI * 2)
			if (task != null) task.cancel();

		for (int k = 0; k < 3; k++) {
			t += Math.PI / 10;
			loc.getWorld().spawnParticle(particle, loc.clone().add(r * Math.cos(t), t / Math.PI / 2, r * Math.sin(t)), 0);
		}
	}
}
