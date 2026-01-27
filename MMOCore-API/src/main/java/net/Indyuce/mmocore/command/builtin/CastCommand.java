package net.Indyuce.mmocore.command.builtin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CastCommand extends CommandTreeRoot {
    public CastCommand(ConfigurationSection config) {
        super(config);
    }

    @Override
    @NotNull
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return explorer.fail("This command is only for players");

        final var playerData = PlayerData.get((Player) sender);
        if (playerData.isCasting()) playerData.leaveSkillCasting();
        else {

            // No skill to cast
            if (!playerData.hasActiveSkillBound()) {
                Message.NO_CLASS_SKILL.send((Player) sender);
                return CommandResult.FAILURE;
            }

            playerData.setSkillCasting();
        }

        return CommandResult.SUCCESS;
    }
}
