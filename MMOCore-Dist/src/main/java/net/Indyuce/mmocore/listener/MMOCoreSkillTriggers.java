package net.Indyuce.mmocore.listener;

import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.skill.trigger.MMOCoreTriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MMOCoreSkillTriggers implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatUpdate(PlayerCombatEvent event) {
        final var caster = event.getData().getMMOPlayerData();
        caster.triggerSkills(new TriggerMetadata(caster, event.entersCombat() ? MMOCoreTriggerType.ENTER_COMBAT : MMOCoreTriggerType.QUIT_COMBAT));
    }
}
