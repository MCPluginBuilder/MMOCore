package net.Indyuce.mmocore.command.rpg.waypoint;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class WaypointsCommandTreeNode extends CommandTreeNode {
	public WaypointsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "waypoints");

		addChild(new UnlockCommandTreeNode(this));
		addChild(new OpenCommandTreeNode(this));
		addChild(new TeleportCommandTreeNode(this));
		addChild(new ItemCommandTreeNode(this));
		addChild(new LockCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
