package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import org.jetbrains.annotations.Nullable;

public class BindSkillTrigger extends Trigger implements Removable {
    private final SkillHandler<?> skill;
    private final int slot;

    public BindSkillTrigger(MMOLineConfig config) {
        super(config);

        slot = config.integer("slot");
        skill = MythicLib.plugin.getSkills().getHandlerOrThrow(config.string("skill"));
    }

    @Override
    public void apply(PlayerData playerData) {
        final @Nullable var found = playerData.getProfess().getSkill(skill);
        if (found != null) playerData.bindSkill(slot, found);
    }

    @Override
    public void remove(PlayerData playerData) {
        playerData.unbindSkill(slot);
    }
}
