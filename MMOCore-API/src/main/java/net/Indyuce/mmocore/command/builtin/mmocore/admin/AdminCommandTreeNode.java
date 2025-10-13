package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.command.CommandTreeNode;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.command.builtin.mmocore.AttributeCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.admin.reset.ResetCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.skill.SkillCommandTreeNode;
import net.Indyuce.mmocore.command.builtin.mmocore.slot.SlotCommandTreeNode;

public class AdminCommandTreeNode extends CommandTreeNode {
    public AdminCommandTreeNode(CommandTreeNode parent) {
        super(parent, "admin");

        addChild(new HideActionBarCommandTreeNode(this));
        addChild(new NoCooldownCommandTreeNode(this));
        addChild(new ResetCommandTreeNode(this));
        addChild(new InfoCommandTreeNode(this));
        addChild(new LegacyClassCommandTreeNode(this));
        addChild(new LegacyForceClassCommandTreeNode(this));
        addChild(new ExportDataTreeNode(this));

        addChild(new ExperienceCommandTreeNode(this));
        addChild(new LevelCommandTreeNode(this));
        addChild(new AttributeCommandTreeNode(this)); // Backwards compatibility
        addChild(new SkillCommandTreeNode(this)); // Backwards compatibility
        addChild(new SaveDataTreeNode(this));
        addChild(new SlotCommandTreeNode(this));
        addChild(new PointsCommandTreeNode("skill", this,
                PlayerData::setSkillPoints,
                PlayerData::giveSkillPoints,
                PlayerData::getSkillPoints));
        addChild(new PointsCommandTreeNode("class", this,
                PlayerData::setClassPoints,
                PlayerData::giveClassPoints,
                PlayerData::getClassPoints));
        addChild(new PointsCommandTreeNode("attribute", this,
                PlayerData::setAttributePoints,
                PlayerData::giveAttributePoints,
                PlayerData::getAttributePoints));
        addChild(new PointsCommandTreeNode("attr-realloc", this,
                PlayerData::setAttributeReallocationPoints,
                PlayerData::giveAttributeReallocationPoints,
                PlayerData::getAttributeReallocationPoints));
        addChild(new PointsCommandTreeNode("skill-realloc", this,
                PlayerData::setSkillReallocationPoints,
                PlayerData::giveSkillReallocationPoints,
                PlayerData::getSkillReallocationPoints));
        addChild(new PointsCommandTreeNode("skill-tree-realloc", this,
                PlayerData::setSkillTreeReallocationPoints,
                PlayerData::giveSkillTreeReallocationPoints,
                PlayerData::getSkillTreeReallocationPoints));
        addChild(new SkillTreePointsCommandTreeNode(this,
                (playerData, amount, skillTree) -> playerData.setSkillTreePoints(skillTree, amount),
                (playerData, amount, skillTree) -> playerData.giveSkillTreePoints(skillTree, amount),
                PlayerData::getSkillTreePoints));
        for (PlayerResource res : PlayerResource.values())
            addChild(new ResourceCommandTreeNode(res.name().toLowerCase(), this, res));
    }
}
