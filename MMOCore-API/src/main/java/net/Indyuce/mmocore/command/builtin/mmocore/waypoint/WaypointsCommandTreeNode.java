package net.Indyuce.mmocore.command.builtin.mmocore.waypoint;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class WaypointsCommandTreeNode extends CommandTreeNode {
	public WaypointsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "waypoints");

		addChild(new UnlockCommandTreeNode(this));
		addChild(new OpenCommandTreeNode(this));
		addChild(new TeleportCommandTreeNode(this));
		addChild(new ItemCommandTreeNode(this));
		addChild(new LockCommandTreeNode(this));
	}
}
