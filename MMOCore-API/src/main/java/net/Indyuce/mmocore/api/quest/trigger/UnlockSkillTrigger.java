package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.jetbrains.annotations.Nullable;

public class UnlockSkillTrigger extends Trigger implements Removable {
    private final SkillHandler<?> skill;

    public UnlockSkillTrigger(MMOLineConfig config) {
        super(config);

        skill = MythicLib.plugin.getSkills().getHandler(config.string("skill"));
    }

    @Override
    public void apply(PlayerData playerData) {
        final @Nullable ClassSkill found = playerData.getProfess().getSkill(skill);
        if (found != null) playerData.unlock(found);
    }

    @Override
    public void remove(PlayerData playerData) {
        final @Nullable ClassSkill found = playerData.getProfess().getSkill(skill);
        if (found != null) playerData.lock(found);
    }
}
