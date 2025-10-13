package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import net.Indyuce.mmocore.command.builtin.mmocore.admin.AdminCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.booster.BoosterCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.clazz.ClassCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.debug.DebugCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.quest.QuestCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.skill.SkillCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.tree.SkillTreeCommandNode;
import net.Indyuce.mmocore.command.builtin.mmocore.waypoint.WaypointsCommandTreeNode;

public class MMOCoreCommandTreeRoot extends CommandTreeRoot {
    public MMOCoreCommandTreeRoot() {
        super("mmocore", "mmocore.admin");

        addChild(new ReloadCommandTreeNode(this));
        addChild(new CastCommandTreeNode(this));
        addChild(new CoinsCommandTreeNode(this));
        addChild(new NoteCommandTreeNode(this));
        addChild(new SkillTreeCommandNode(this));
        addChild(new AdminCommandTreeNode(this));
        addChild(new DebugCommandTreeNode(this));
        addChild(new BoosterCommandTreeNode(this));
        addChild(new WaypointsCommandTreeNode(this));
        addChild(new QuestCommandTreeNode(this));
        addChild(new SkillCommandTreeNode(this));
        addChild(new AttributeCommandTreeNode(this));
        addChild(new ClassCommandTreeNode(this));
    }
}
