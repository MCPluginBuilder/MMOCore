package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import net.Indyuce.mmocore.command.rpg.CastCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.CoinsCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.NoteCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.ReloadCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.admin.AdminCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.booster.BoosterCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.debug.DebugCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.quest.QuestCommandTreeNode;
import net.Indyuce.mmocore.command.rpg.waypoint.WaypointsCommandTreeNode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public class MMOCoreCommandTreeRoot extends CommandTreeRoot implements CommandExecutor, TabCompleter {
	public MMOCoreCommandTreeRoot() {
		super("mmocore", "mmocore.admin");

		addChild(new ReloadCommandTreeNode(this));
		addChild(new CastCommandTreeNode(this));
		addChild(new CoinsCommandTreeNode(this));
		addChild(new NoteCommandTreeNode(this));
		addChild(new AdminCommandTreeNode(this));
		addChild(new DebugCommandTreeNode(this));
		addChild(new BoosterCommandTreeNode(this));
		addChild(new WaypointsCommandTreeNode(this));
		addChild(new QuestCommandTreeNode(this));
	}
}
