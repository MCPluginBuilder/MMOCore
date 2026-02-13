package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InfoCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;

    public InfoCommandTreeNode(CommandTreeNode parent) {
        super(parent, "info");

        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);

        PlayerData playerData = PlayerData.get(player);
        explorer.verbose("&e----------------------------------------------------");
        explorer.verbose("&eClass: &6" + playerData.getProfess().getName());
        explorer.verbose("&eLevel: &6" + playerData.getLevel());
        explorer.verbose("&eExperience: &6" + MythicLib.plugin.getMMOConfig().decimal.format(playerData.getExperience()) + "&e / &6" + playerData.getLevelUpExperience());
        explorer.verbose("&eClass Points: &6" + playerData.getClassPoints());
        explorer.verbose("&eQuests: &6" + playerData.getQuestData().getFinishedQuests().size() + "&e / &6" + MMOCore.plugin.questManager.getAll().size());
        explorer.verbose("&e----------------------------------------------------");
        for (var profession : MMOCore.plugin.professionManager.getAll())
            explorer.verbose("&e" + profession.getName() + ": Lvl &6" + playerData.getCollectionSkills().getLevel(profession)
                    + "&e - &6" + MythicLib.plugin.getMMOConfig().decimal.format(playerData.getCollectionSkills().getExperience(profession))
                    + "&e / &6" + playerData.getCollectionSkills().getLevelUpExperience(profession));
        explorer.verbose("&e----------------------------------------------------");
        return CommandResult.SUCCESS;
    }
}
