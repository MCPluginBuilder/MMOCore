package net.Indyuce.mmocore.quest;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface QuestModule {

    /**
     * @return Quest with given identifier
     */
    @Nullable
    public AbstractQuest getQuestOrThrow(String id);

    /**
     * @return If a specific player has made a certain quest
     */
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId);
}
