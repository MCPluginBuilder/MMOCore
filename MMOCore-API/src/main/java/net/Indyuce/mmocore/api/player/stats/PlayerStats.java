package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.skill.ClassSkill;

// TODO merge with PlayerData? not really needed class
public class PlayerStats {
    private final PlayerData data;

    /**
     * Util class to easily manipulate the MythicLib stat map
     *
     * @param data Player
     */
    public PlayerStats(PlayerData data) {
        this.data = data;
    }

    public PlayerData getData() {
        return data;
    }

    public StatMap getMap() {
        return data.getMMOPlayerData().getStatMap();
    }

    @Deprecated
    public StatInstance getInstance(StatType stat) {
        return getMap().getInstance(stat.name());
    }

    @Deprecated
    public StatInstance getInstance(String stat) {
        return getMap().getInstance(stat);
    }

    public double getStat(String stat) {
        return getMap().getStat(stat);
    }

    /**
     * MMOCore base stat value differs from the one in MythicLib.
     * <p>
     * MythicLib: the base stat value is only defined for stats
     * which are based on vanilla player attributes. It corresponds
     * to the stat amount any player has with NO attribute modifier whatsoever.
     * <p>
     * MMOCore: the base stat value corresponds to the stat amount
     * the player CLASS grants. It can be similar or equal to the one
     * in MMOCore but it really is completely different.
     *
     * @return MMOCore base stat value
     */
    public double getBase(String stat) {
        final Profession profession = StatInfo.valueOf(stat).profession;
        return data.getProfess().calculateBaseStat(stat, profession == null ? data.getLevel() : data.getCollectionSkills().getLevel(profession), data);
    }

    @Deprecated
    public void updateStats(boolean ignored) {
        this.updateStats();
    }

    private static final String MODIFIER_KEY = "MMOCoreClass";

    /**
     * Used to update MMOCore stat modifiers due to class and send them over to
     * MythicLib. Must be ran everytime the player levels up, changes class or
     * when the plugin reloads.
     */
    public synchronized void updateStats() {

        // Update player stats
        getMap().bufferUpdates(() -> {
            for (String stat : MMOCore.plugin.statManager.getRegistered()) {
                final var instance = getMap().getInstance(stat);

                // Remove modifiers due to class
                instance.removeIf(MODIFIER_KEY::equals);

                // Add newest one
                final double total = getBase(instance.getStat()) - instance.getDefaultBase();
                if (total != 0)
                    instance.registerModifier(new StatModifier(MODIFIER_KEY, instance.getStat(), total, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));
            }
        });

        // Updates the player's UNBINDABLE PASSIVE (== PERMANENT) skills
        final PassiveSkillMap skillMap = data.getMMOPlayerData().getPassiveSkillMap();
        skillMap.removeModifiers("MMOCorePermanentSkill");
        for (ClassSkill skill : data.getProfess().getSkills())
            if (skill.isPermanent()
                    && skill.getTrigger() != TriggerType.LOGIN
                    && data.hasUnlocked(skill)
                    && data.hasUnlockedLevel(skill))
                skillMap.addModifier(skill.toPassive(data));

        // Updates the player's CLASS scripts
        skillMap.removeModifiers("MMOCoreClassScript");
        for (PassiveSkill script : data.getProfess().getScripts())
            if (script.getType() != TriggerType.LOGIN) skillMap.addModifier(script);
    }
}
