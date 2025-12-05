package net.Indyuce.mmocore.skill.list;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"extra"}, triggerable = false)
public class Neptune_Gift extends SkillHandler<SimpleSkillResult> implements Listener {
    public Neptune_Gift(ConfigurationSection config) {
        super(config);
    }

    @NotNull
    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        throw new RuntimeException("Not supported");
    }

    @EventHandler(ignoreCancelled = true)
    public void a(PlayerResourceUpdateEvent event) {
        if (event.getReason().isRegeneration()) return;
        if (event.getPlayer().getLocation().getBlock().getType() != Material.WATER) return;

        final var skill = event.getData().getMMOPlayerData().getPassiveSkillMap().getSkill(this);
        if (skill == null) return; // No skill

        final var regenerated = event.getDifference();
        if (regenerated < 0) return; // WTH? loosing resource

        final var extraModifier = event.getData().getMMOPlayerData().getSkillModifierMap().calculateValue(skill.getTriggeredSkill(), "extra");
        final var extraRegen = regenerated * extraModifier / 100;
        event.setNewAmount(event.getNewAmount() + extraRegen);
    }
}
