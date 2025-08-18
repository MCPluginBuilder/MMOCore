package net.Indyuce.mmocore.comp.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardRegionHandler implements RegionHandler {

	@Override
	public List<String> getRegions(Location loc) {
		List<String> regions = new ArrayList<>();
		WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(loc)).getRegions().forEach(region -> regions.add(region.getId()));
		return regions;
	}
}
