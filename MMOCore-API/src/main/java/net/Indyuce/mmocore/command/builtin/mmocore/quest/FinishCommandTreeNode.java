package net.Indyuce.mmocore.command.builtin.mmocore.quest;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FinishCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;

    public FinishCommandTreeNode(CommandTreeNode parent) {
        super(parent, "finish");

        argPlayer = addArgument(Argument.PLAYER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);

        PlayerQuests quests = PlayerData.get(player).getQuestData();
        if (!quests.hasCurrent()) return explorer.success(player.getName() + " has no ongoing quest.");

        // Complete all objectives
        while (quests.hasCurrent()) quests.getCurrent().completeObjective();

        return explorer.success(player.getName() + " has completed their ongoing quest.");
    }
}
