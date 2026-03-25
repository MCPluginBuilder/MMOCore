package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.message.PlayerMessage;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import net.Indyuce.mmocore.skill.cast.Keybind;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import net.Indyuce.mmocore.skill.cast.SkillCastingInstance;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SkillBar extends SkillCastingHandler {
    private final Keybind mainKey;
    private final boolean ignoreSneak, lowestKeybinds;
    private final ActionBarOptions actionBarOptions;

    // Messages
    @NotNull
    private final PlayerMessage messageEnter, messageQuit;

    public SkillBar(@NotNull ConfigurationSection config) {
        super(config);

        mainKey = Objects.requireNonNull(Keybind.fromConfig(config.get("open")), "Could not find open keybind");
        ignoreSneak = config.getBoolean("ignore-sneak", config.getBoolean("disable-sneak", false));
        lowestKeybinds = config.getBoolean("use-lowest-keybinds");

        // Messages
        messageEnter = PlayerMessage.fromConfig(config.get("message.enter"));
        messageQuit = PlayerMessage.fromConfig(config.get("message.quit"));

        // Action bar
        actionBarOptions = config.contains("action-bar") ? new ActionBarOptions(config.getConfigurationSection("action-bar")) : null;
    }

    @NotNull
    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @NotNull
    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.SKILL_BAR;
    }

    @Override
    public void onSkillBindChange(@NotNull PlayerData player) {

        // Lowest indices = start at slot 1 and increase
        if (lowestKeybinds) {

            int slot = 1;

            for (BoundSkillInfo bound : player.getBoundSkills().values())
                // Set cast slot and increment slot
                if (!bound.isPassive()) bound.skillBarCastSlot = slot++;
        }

        // Otherwise, direct correspondence
        else player.getBoundSkills().forEach((slot, bound) -> bound.skillBarCastSlot = slot);
    }

    @EventHandler
    public void enterSkillCasting(PlayerKeyPressEvent event) {
        if (!mainKey.matches(event)) return;

        // Extra option to improve support with other plugins
        final Player player = event.getPlayer();
        if (ignoreSneak && player.isSneaking()) return;

        // Always cancel event
        if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

        // Enter spell casting
        final PlayerData playerData = event.getData();
        if (player.getGameMode() != GameMode.SPECTATOR
                && (MMOCore.plugin.configManager.canCreativeCast || player.getGameMode() != GameMode.CREATIVE)
                && !playerData.isCasting()
                && playerData.hasActiveSkillBound())
            if (playerData.setSkillCasting())
                messageEnter.send(playerData.getMMOPlayerData());
    }

    public class CustomSkillCastingInstance extends SkillCastingInstance {
        CustomSkillCastingInstance(PlayerData playerData) {
            super(SkillBar.this, playerData);
        }

        @EventHandler
        public void onItemHeld(PlayerItemHeldEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;

            // Extra option to improve support with other plugins
            final Player player = event.getPlayer();
            if (ignoreSneak && player.isSneaking()) return;

            /*
             * When the event is cancelled, another playerItemHeldEvent is
             * called and previous and next slots are equal. the event must not
             * listen to that non-player called event.
             */
            if (event.getPreviousSlot() == event.getNewSlot()) return;

            event.setCancelled(true);
            refreshTimeOut();

            // Look for skill with given slot
            ClassSkill classSkill = findSkillToCast(player.getInventory().getHeldItemSlot(), event.getNewSlot());
            if (classSkill != null) classSkill.toCastable(getCaster()).cast(getCaster().getMMOPlayerData());
        }

        @Nullable
        private ClassSkill findSkillToCast(int currentSlot, int clickedSlot) {
            for (BoundSkillInfo info : this.getActiveSkills())
                if (info.skillBarCastSlot + (currentSlot < info.skillBarCastSlot ? 1 : 0) == 1 + clickedSlot)
                    return info.getClassSkill();
            return null;
        }

        @EventHandler
        public void stopCasting(PlayerKeyPressEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;

            if (!mainKey.matches(event)) return;

            // Extra option to improve support with other plugins
            final Player player = event.getPlayer();
            if (ignoreSneak && player.isSneaking()) return;

            if (getCaster().leaveSkillCasting()) messageQuit.send(caster.getMMOPlayerData());
        }

        /**
         * We don't even need to check if the skill has the 'cooldown'
         * modifier. We just look for an entry in the cooldown map which
         * won't be here if the skill has no cooldown.
         */
        private boolean onCooldown(ClassSkill skill) {
            return caster.getCooldownMap().isOnCooldown(skill);
        }

        private boolean noMana(ClassSkill skill) {
            return skill.getParameter("mana", caster) > caster.getMana();
        }

        private boolean noStamina(ClassSkill skill) {
            return skill.getParameter("stamina", caster) > caster.getStamina();
        }

        @Override
        public void onTick() {
            if (actionBarOptions == null) return; // No action bar to display
            if ((counter & 0b1111) != 0) return; // Don't display it too often, useless

            caster.getMMOPlayerData().getActionBar().show(ACTION_BAR_PRIORITY, () -> actionBarOptions.format(this));
        }

        @Override
        protected void onClose() {
            caster.getMMOPlayerData().getActionBar().reset(ACTION_BAR_PRIORITY);
        }
    }

    public class ActionBarOptions {
        private final String ready, onCooldown, noMana, noStamina, split;

        public ActionBarOptions(@NotNull ConfigurationSection config) {
            this.ready = config.getString("ready");
            this.split = config.getString("split");
            this.onCooldown = config.getString("on-cooldown");
            this.noMana = config.getString("no-mana");
            this.noStamina = config.getString("no-stamina");
        }

        @NotNull
        public String format(CustomSkillCastingInstance instance) {

            // Failsafe
            if (!instance.getCaster().isOnline()) return "";

            var data = instance.getCaster();
            final var builder = new StringBuilder();
            for (BoundSkillInfo active : instance.getActiveSkills()) {
                final ClassSkill skill = active.getClassSkill();
                final int slot = active.skillBarCastSlot;

                if (!builder.isEmpty()) builder.append(split);

                String singleSkill;
                if (instance.onCooldown(skill))
                    singleSkill = onCooldown.replace("{cooldown}", String.valueOf(data.getCooldownMap().getInfo(skill).getRemaining() / 1000));
                else if (instance.noMana(skill)) singleSkill = noMana;
                else if (instance.noStamina(skill)) singleSkill = noStamina;
                else singleSkill = ready;

                builder.append(singleSkill
                        .replace("{index}", String.valueOf(slot + (data.getPlayer().getInventory().getHeldItemSlot() < slot ? 1 : 0)))
                        .replace("{skill}", skill.getSkill().getName()));
            }

            return MythicLib.plugin.getPlaceholderParser().parse(data.getPlayer(), builder.toString());
        }
    }
}
