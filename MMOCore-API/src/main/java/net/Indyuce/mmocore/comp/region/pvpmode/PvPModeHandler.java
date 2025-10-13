package net.Indyuce.mmocore.comp.region.pvpmode;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.comp.flags.WorldGuardFlags;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.player.CombatHandler;
import net.Indyuce.mmocore.player.Message;

import java.util.Objects;

public class PvPModeHandler extends MMOCoreFlagHandler {

    public static final Factory<PvPModeHandler> FACTORY = new Factory<>() {
        final WorldGuardFlags wgFlags = Objects.requireNonNull(MythicLib.plugin.getFlags().getHandler(WorldGuardFlags.class), "Could not reach ML compatibility class for WG");

        @Override
        public PvPModeHandler create(Session session) {
            return new PvPModeHandler(session, wgFlags.toWorldGuard(CustomFlag.PVP_MODE));
        }
    };

    public PvPModeHandler(Session session, StateFlag flag) {
        super(session, flag);
    }

    @Override
    public State getDefaultState() {
        return State.ALLOW;
    }

    /**
     * Triggered when a player changes region and finds a new value for that flag.
     * In that case, apply the new setting and display messages if needed.
     */
    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
        if (isInvalid())
            return true;

        boolean newPvpMode = toBoolean(currentValue);
        boolean lastPvpMode = toBoolean(lastValue);

        if (!newPvpMode && lastPvpMode) {

            // Apply cooldown
            playerData.getMMOPlayerData().getCooldownMap().applyCooldown(CombatHandler.COOLDOWN_KEY, MMOCore.plugin.configManager.pvpModeRegionLeaveCooldown);

            // Send message
            if (canSendMessage()) {

                // Leave combat when joining safe zone
                final boolean pvpFlag = toSet.queryState(null, Flags.PVP) != StateFlag.State.DENY;
                if (playerData.getCombat().isInPvpMode() && !pvpFlag)
                    playerData.getCombat().close();

                lastMessage = System.currentTimeMillis();
                final boolean pvpEnabled = playerData.getCombat().isInPvpMode() && !playerData.getCombat().canQuitPvpMode() && pvpFlag;
                final double remaining = (playerData.getCombat().getLastHit() + MMOCore.plugin.configManager.pvpModeCombatTimeout * 1000.0D - System.currentTimeMillis()) / 1000.0D;
                final var remainingFormatted = MythicLib.plugin.getMMOConfig().decimal.format(remaining);
                final var message = pvpEnabled ? Message.PVP_MODE_LEAVE_PVP_ALLOWED : Message.PVP_MODE_LEAVE_PVP_DENIED;
                message.send(playerData, "remaining", remainingFormatted);
            }
        } else if (newPvpMode && !lastPvpMode) {

            // Apply cooldown
            playerData.getMMOPlayerData().getCooldownMap().applyCooldown(CombatHandler.COOLDOWN_KEY, MMOCore.plugin.configManager.pvpModeRegionEnterCooldown);

            // Apply invulnerability
            final boolean applyInvulnerability = playerData.getCombat().isInPvpMode() && playerData.getCombat().canQuitPvpMode();
            if (applyInvulnerability)
                playerData.getCombat().setInvulnerable(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeRegionChange);

            // Send message
            if (canSendMessage()) {
                lastMessage = System.currentTimeMillis();
                final var message = applyInvulnerability ? Message.PVP_MODE_ENTER_PVP_MODE_ON : Message.PVP_MODE_ENTER_PVP_MODE_OFF;
                final var timeFormatted = MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeRegionChange);
                message.send(playerData, "time", timeFormatted);
            }
        }
        return true;
    }
}
