package net.Indyuce.mmocore.command.builtin.mmocore.debug;

import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.ChatColor;

public class DebugCommandTreeNode extends CommandTreeNode {
	public static final String commandPrefix = ChatColor.YELLOW + "[" + ChatColor.RED + "DEBUG" + ChatColor.GOLD + "] " + ChatColor.RESET;

	public DebugCommandTreeNode(CommandTreeNode parent) {
		super(parent, "debug");

		addChild(new StatValueCommandTreeNode(this));
		addChild(new StatModifiersCommandTreeNode(this));
	}
}
