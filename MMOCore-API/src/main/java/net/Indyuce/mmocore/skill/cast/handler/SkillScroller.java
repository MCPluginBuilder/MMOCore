package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.SoundObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.cast.Keybind;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import net.Indyuce.mmocore.skill.cast.SkillCastingInstance;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SkillScroller extends SkillCastingHandler {

    private final Keybind enterKey, castKey, scrollKey, scrollBackKey;

    @Nullable
    private final SoundObject enterSound, changeSound, changeBackSound, leaveSound;

    private final String actionBarFormat;

    private final boolean ignoreSneak, quitOnCast, quitOnSwitchEmpty;

    public SkillScroller(@NotNull ConfigurationSection config) {
        super(config);

        // Load sounds
        enterSound = config.contains("sound.enter") ? new SoundObject(config.getConfigurationSection("sound.enter")) : null;
        changeSound = config.contains("sound.change") ? new SoundObject(config.getConfigurationSection("sound.change")) : null;
        changeBackSound = config.contains("sound.change-back") ? new SoundObject(config.getConfigurationSection("sound.change-back")) : null;
        leaveSound = config.contains("sound.leave") ? new SoundObject(config.getConfigurationSection("sound.leave")) : null;

        actionBarFormat = config.getString("action-bar-format", "CLICK TO CAST: {selected}");
        ignoreSneak = config.getBoolean("ignore-sneak");
        quitOnCast = config.getBoolean("quit-on-cast");
        quitOnSwitchEmpty = config.getBoolean("quit-on-switch-empty-hand");

        // Find keybinds
        enterKey = Objects.requireNonNull(Keybind.fromConfig(config.get("enter-key")), "Could not find enter key");
        castKey = Objects.requireNonNull(Keybind.fromConfig(config.get("cast-key")), "Could not find cast key");
        scrollKey = Keybind.fromConfig(config.get("scroll-key"));
        scrollBackKey = Keybind.fromConfig(config.get("scroll-back-key"));
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.SKILL_SCROLLER;
    }

    @EventHandler
    public void whenPressingKey(PlayerKeyPressEvent event) {
        var playerData = event.getData();
        var player = playerData.getPlayer();

        // Disable creative
        if (player.getGameMode() == GameMode.CREATIVE && !MMOCore.plugin.configManager.canCreativeCast) return;

        // Extra option to improve support with other plugins
        if (ignoreSneak && player.isSneaking()) return;

        if (enterKey.matches(event)) {

            // Leave casting mode
            if (playerData.isCasting()) {

                // Cancel event if necessary
                if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

                if (!playerData.leaveSkillCasting()) return;

                if (leaveSound != null) leaveSound.playTo(player);
                return;
            }

            // Check if there are skills bound
            if (!playerData.hasActiveSkillBound()) return;

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

            // Enter casting mode
            if (!playerData.setSkillCasting()) return;

            if (enterSound != null) enterSound.playTo(player);
        }

        if (castKey.matches(event) && playerData.isCasting()) {

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

            // Cast skill
            var casting = (CustomSkillCastingInstance) playerData.getSkillCasting();
            var caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
            var result = casting.getSelected().toCastable(playerData).cast(new TriggerMetadata(caster, null, null));

            // Quit on cast? Only if successful
            if (result.isSuccessful() && quitOnCast) playerData.leaveSkillCasting();
        }
    }

    public class CustomSkillCastingInstance extends SkillCastingInstance {
        CustomSkillCastingInstance(PlayerData caster) {
            super(SkillScroller.this, caster);
        }

        @Override
        public void onTick() {
            final String skillName = getSelected().getSkill().getName();
            final String actionBarFormat = MythicLib.plugin.getPlaceholderParser().parse(getCaster().getPlayer(), SkillScroller.this.actionBarFormat.replace("{selected}", skillName));
            getCaster().displayActionBar(actionBarFormat);
        }

        public ClassSkill getSelected() {
            return getActiveSkills().get(caster.permSkillScrollIndex).getClassSkill();
        }

        @EventHandler
        public void onKeyPress(PlayerKeyPressEvent event) {
            if (scrollKey == null) return; // Only when mouse scrolling is enabled
            if (!event.getData().equals(getCaster())) return; // Player check
            if (ignoreSneak && event.getPlayer().isSneaking()) return; // Improves compatibility with other plugins

            // Quit scrolling if no skill bound
            if (!caster.hasActiveSkillBound()) {
                caster.leaveSkillCasting(true);
                return;
            }

            // Find scroll direction
            int delta;
            if (scrollKey.matches(event)) delta = 1;
            else if (scrollBackKey != null && scrollBackKey.matches(event)) delta = -1;
            else return;

            event.setCancelled(true);
            scrollOf(delta);
        }

        @EventHandler
        public void onScroll(PlayerItemHeldEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return; // Player check

            // Quit if switching to empty hand
            if (quitOnSwitchEmpty && UtilityMethods.isAir(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
                caster.leaveSkillCasting(true);
                return;
            }

            if (scrollKey != null) return; // Only when mouse scrolling is disabled
            if (ignoreSneak && event.getPlayer().isSneaking()) return; // Improves compatibility with other plugins

            // Quit scrolling if no skill bound
            if (!caster.hasActiveSkillBound()) {
                caster.leaveSkillCasting(true);
                return;
            }

            event.setCancelled(true);

            final int previous = event.getPreviousSlot(), current = event.getNewSlot();
            final int dist1 = 9 + current - previous, dist2 = current - previous, dist3 = current - previous - 9;
            final int change = Math.abs(dist1) < Math.abs(dist2) ? (Math.abs(dist1) < Math.abs(dist3) ? dist1 : dist3) : (Math.abs(dist3) < Math.abs(dist2) ? dist3 : dist2);
            scrollOf(change); // Scroll
        }

        private void scrollOf(int delta) {

            // Safeguard, filter out useless scrolls
            if (delta == 0) return;

            caster.permSkillScrollIndex = remainder(caster.permSkillScrollIndex + delta, getActiveSkills().size());
            this.onTick();
            this.refreshTimeOut();

            // Play sound
            var sound = delta > 0 || changeBackSound == null ? changeSound : changeBackSound; // Select sound
            if (sound != null) sound.playTo(caster.getPlayer());
        }
    }

    private static int remainder(int x, int n) {
        while (x < 0) x += n;
        while (x >= n) x -= n;
        return x;
    }
}
