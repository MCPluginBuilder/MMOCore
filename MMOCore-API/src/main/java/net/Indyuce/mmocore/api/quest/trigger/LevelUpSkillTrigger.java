package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;

public class LevelUpSkillTrigger extends Trigger implements Removable {
    private final SkillHandler<?> skill;
    private final int amount;

    public LevelUpSkillTrigger(MMOLineConfig config) {
        super(config);

        amount = config.integer("amount");
        skill = MythicLib.plugin.getSkills().getHandlerOrThrow(config.string("skill"));
    }

    @Override
    public void apply(PlayerData playerData) {
        playerData.setSkillLevel(skill, playerData.getSkillLevel(skill) + amount);
    }

    @Override
    public void remove(PlayerData playerData) {
        playerData.setSkillLevel(skill, Math.max(0, playerData.getSkillLevel(skill) - amount));
    }
}
