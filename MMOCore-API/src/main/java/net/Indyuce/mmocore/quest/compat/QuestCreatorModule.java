package net.Indyuce.mmocore.quest.compat;

import com.guillaumevdn.questcreator.ConfigQC;
import com.guillaumevdn.questcreator.data.user.QuestHistoryElement;
import com.guillaumevdn.questcreator.data.user.UserQC;
import com.guillaumevdn.questcreator.lib.model.ElementModel;
import com.guillaumevdn.questcreator.lib.quest.QuestEndType;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.quest.AbstractQuest;
import net.Indyuce.mmocore.quest.QuestModule;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class QuestCreatorModule implements QuestModule {

    @Override
    public QuestImpl getQuestOrThrow(String id) {
        return new QuestImpl(id);
    }

    @Override
    public boolean hasCompletedQuest(@NotNull MMOPlayerData playerData, @NotNull String questId) {
        UserQC qcData = UserQC.cachedOrNull(playerData.getUniqueId());
        if (qcData == null) return false;

        //Gets all the quests the player has  succeeded at
        // Linear scan
        List<QuestHistoryElement> elements = qcData.getQuestHistory().getElements(questId, Arrays.asList(QuestEndType.SUCCESS), 0);
        for (QuestHistoryElement el : elements)
            if (el.getModelId().equals(questId))
                return true;

        return false;
    }

    /**
     * QC ElementModel corresponds to our quest and their
     * quests correspond to our Quest progress class
     */

    public static class QuestImpl implements AbstractQuest {
        final ElementModel questModel;

        public QuestImpl(String modelId) {
            questModel = ConfigQC.models.getElement(modelId).orNull();
        }

        @Override
        public String getName() {
            return questModel.getDisplayName().getId();
        }

        @Override
        public String getId() {
            return questModel.getId();
        }
    }
}
