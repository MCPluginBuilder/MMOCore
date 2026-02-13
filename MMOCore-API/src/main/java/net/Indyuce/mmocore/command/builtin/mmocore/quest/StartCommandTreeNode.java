package net.Indyuce.mmocore.command.builtin.mmocore.quest;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Quest> argQuest;

    public StartCommandTreeNode(CommandTreeNode parent) {
        super(parent, "start");

        argPlayer = addArgument(Argument.PLAYER);
        argQuest = addArgument(Arguments.QUEST);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        Quest quest = explorer.parse(argQuest);

        PlayerQuests quests = PlayerData.get(player).getQuestData();
        if (quests.hasCurrent()) return explorer.success(player.getName() + " already has an ongoing quest.");

        quests.start(quest);
        return explorer.success(player.getName() + " has started " + quest.getName() + ".");
    }
}
