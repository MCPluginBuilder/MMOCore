package net.Indyuce.mmocore.quest.provided;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.quest.AbstractQuest;
import net.Indyuce.mmocore.quest.QuestModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoneQuestModule implements QuestModule {

    @Override
    public @Nullable AbstractQuest getQuestOrThrow(String id) {
        throw new IllegalStateException("Quests are disabled");
    }

    @Override
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId) {
        return false;
    }
}
