package net.Indyuce.mmocore.quest.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.quests.Quest;
import net.Indyuce.mmocore.quest.AbstractQuest;
import net.Indyuce.mmocore.quest.QuestModule;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class QuestsModule implements QuestModule {
    private final BukkitQuestsPlugin plugin = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin("Quests");

    @Override
    public QuestImpl getQuestOrThrow(String id) {
        final var found = plugin.getQuest(id);
        return found == null ? null : new QuestImpl(found);
    }

    @Override
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId) {
        var quester = plugin.getQuester(playerData.getUniqueId());
        if (quester == null) return false;

        for (var quest : quester.getCompletedQuests())
            if (quest.getId().equals(questId)) return true;

        return false;
    }

    public static class QuestImpl implements AbstractQuest {
        private final Quest quest;

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
