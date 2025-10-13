package net.Indyuce.mmocore.command.builtin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.player.CombatHandler;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvpModeCommand extends CommandTreeRoot {
    private final Argument<Player> argPlayer;

    public PvpModeCommand(ConfigurationSection config) {
        super(config);

        argPlayer = addArgument(Arguments.PLAYER_IF_OP);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);

        final PlayerData playerData = PlayerData.get(player);
        if (MMOCoreUtils.callLegacyCommandEvent(playerData, this)) return CommandResult.FAILURE;

        // Check command cooldown
        if (playerData.getCooldownMap().isOnCooldown(CombatHandler.COOLDOWN_KEY)) {
            var remainingFormatted = MythicLib.plugin.getMMOConfig().decimal.format(playerData.getCooldownMap().getCooldown(CombatHandler.COOLDOWN_KEY));
            Message.PVP_MODE_COOLDOWN.send(playerData, "remaining", remainingFormatted);
            return CommandResult.FAILURE;
        }

        playerData.getCombat().setPvpMode(!playerData.getCombat().isInPvpMode());
        playerData.getCooldownMap().applyCooldown(CombatHandler.COOLDOWN_KEY, playerData.getCombat().isInPvpMode() ? MMOCore.plugin.configManager.pvpModeToggleOnCooldown : MMOCore.plugin.configManager.pvpModeToggleOffCooldown);

        // Toggling on when in PVP region
        // Give invulnerability for a short time
        if (playerData.getCombat().isInPvpMode() &&
                MythicLib.plugin.getFlags().isFlagAllowed(playerData.getPlayer(), CustomFlag.PVP_MODE)) {
            playerData.getCombat().setInvulnerable(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeCommand);
            var timeFormatted = MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeCommand);
            Message.PVP_MODE_TOGGLE_ON_INVULNERABLE.send((Player) sender, "time", timeFormatted);
        }

        // Just send message otherwise
        else {
            var currentPvpMode = playerData.getCombat().isInPvpMode();
            (currentPvpMode ? Message.PVP_MODE_TOGGLE_ON_SAFE : Message.PVP_MODE_TOGGLE_OFF_SAFE).send((Player) sender);
        }

        return CommandResult.SUCCESS;
    }
}
