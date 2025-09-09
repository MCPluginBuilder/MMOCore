package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PvpModeCommand extends RegisteredCommand {
    public PvpModeCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.PVP_MODE);
    }

    public static final String COOLDOWN_KEY = "PvpMode";

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return false;
        }

        if (!sender.hasPermission("mmocore.pvpmode")) {
            Message.NOT_ENOUGH_PERMS.send((Player) sender);
            return false;
        }

        final PlayerData playerData = PlayerData.get((Player) sender);

        // Command cooldown
        if (playerData.getCooldownMap().isOnCooldown(COOLDOWN_KEY)) {
            var remainingFormatted = MythicLib.plugin.getMMOConfig().decimal.format(playerData.getCooldownMap().getCooldown(COOLDOWN_KEY));
            Message.PVP_MODE_COOLDOWN.send((Player) sender, "remaining", remainingFormatted);
            return true;
        }

        playerData.getCombat().setPvpMode(!playerData.getCombat().isInPvpMode());
        playerData.getCooldownMap().applyCooldown(COOLDOWN_KEY, playerData.getCombat().isInPvpMode() ? MMOCore.plugin.configManager.pvpModeToggleOnCooldown : MMOCore.plugin.configManager.pvpModeToggleOffCooldown);

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

        return true;
    }
}
