package net.Indyuce.mmocore.quest.compat;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import net.Indyuce.mmocore.quest.AbstractQuest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestsModule implements QuestModule<QuestsModule.QuestImpl> {
    private final BukkitQuestsPlugin plugin;

    public QuestsModule() {
        plugin = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin("Quests");
    }

    @Override
    public QuestImpl getQuestOrThrow(String id) {
        final var found = plugin.getQuest(id);
        return found == null ? null : new QuestImpl(found);
    }


    @Override
    public boolean hasCompletedQuest(String questId, Player player) {
        Quester quester = plugin.getQuester(player.getUniqueId());
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
