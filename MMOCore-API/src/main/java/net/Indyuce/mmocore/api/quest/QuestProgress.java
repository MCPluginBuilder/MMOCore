package net.Indyuce.mmocore.api.quest;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.objective.Objective;

public class QuestProgress {
    private final Quest quest;
    private final PlayerData player;

    private int objectiveIndex;
    private ObjectiveProgress objectiveProgress;

    public QuestProgress(Quest quest, PlayerData player) {
        this(quest, player, 0);
    }

    public QuestProgress(Quest quest, PlayerData player, int objective) {
        this.quest = quest;
        this.player = player;

        this.objectiveIndex = objective;
        objectiveProgress = nextObjective().newProgress(this);
    }

    public Quest getQuest() {
        return quest;
    }

    public PlayerData getPlayer() {
        return player;
    }

    public int getObjectiveNumber() {
        return objectiveIndex;
    }

    public ObjectiveProgress getProgress() {
        return objectiveProgress;
    }

    private Objective nextObjective() {
        return quest.getObjectives().get(objectiveIndex);
    }

    public void completeObjective() {
        objectiveIndex++;
        objectiveProgress.close();
        final var finishedObjectiveProgress = objectiveProgress;

        // Start next objective, or end quest.
        if (objectiveIndex >= quest.getObjectives().size()) player.getQuestData().finishCurrent();
        else objectiveProgress = nextObjective().newProgress(this);

        // Update bossbar
        player.getQuestData().updateBossBar();

        /*
         * Apply triggers only at the end! It comes handy when starting another
         * quest in some storyline using triggers from the previous quest.
         */
        finishedObjectiveProgress.getObjective().getTriggers().forEach(trigger -> trigger.schedule(getPlayer()));
    }

    public String getFormattedLore() {
        return MythicLib.plugin.parseColors(objectiveProgress.formatLore(objectiveProgress.getObjective().getDefaultLore()));
    }
}