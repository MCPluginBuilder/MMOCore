package net.Indyuce.mmocore.quest.compat;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.quests.Quest;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.quest.AbstractQuest;
import net.Indyuce.mmocore.quest.QuestModule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BeautyQuestModule implements QuestModule {
    private final QuestsAPI api = QuestsAPI.getAPI();

    @Override
    public AbstractQuest getQuestOrThrow(String questId) {
        var quest = api.getQuestsManager().getQuest(Integer.parseInt(questId));
        Objects.requireNonNull(quest, "Quest not found with ID '" + questId + "'");
        return new QuestImpl(quest);
    }

    @Override
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId) {
        var account = api.getPlugin().getPlayersManager().getAccount(playerData.getPlayer());
        var quest = api.getQuestsManager().getQuest(Integer.parseInt(questId));
        Objects.requireNonNull(quest, "Quest not found with ID '" + questId + "'");
        return account.getQuestDatas(quest).isFinished();
    }

    static class QuestImpl implements AbstractQuest {
        final Quest quest;

        public QuestImpl(Quest quest) {
            this.quest = quest;
        }

        @Override
        public String getName() {
            return quest.getName();
        }

        @Override
        public String getId() {
            return String.valueOf(quest.getId());
        }
    }
}
