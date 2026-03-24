package net.Indyuce.mmocore.quest.provided;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.quest.AbstractQuest;
import net.Indyuce.mmocore.quest.QuestModule;
import org.jetbrains.annotations.NotNull;

public class MMOCoreQuestModule implements QuestModule {

    @Override
    public AbstractQuest getQuestOrThrow(String id) {
        var quest = MMOCore.plugin.questManager.get(id);
        if (quest == null) return null;

        return new QuestImpl(quest);
    }

    @Override
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId) {
        return false;
    }

    public static class QuestImpl implements AbstractQuest {
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
            return quest.getId();
        }
    }
}
