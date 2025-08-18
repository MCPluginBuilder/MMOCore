package net.Indyuce.mmocore.comp.region;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DefaultRegionHandler implements RegionHandler {

	@Override
	public List<String> getRegions(Location loc) {
		return new ArrayList<>();
	}
}
