package net.Indyuce.mmocore.quest;

import net.Indyuce.mmocore.quest.compat.BeautyQuestModule;
import net.Indyuce.mmocore.quest.compat.QuestCreatorModule;
import net.Indyuce.mmocore.quest.compat.QuestsModule;
import net.Indyuce.mmocore.quest.provided.MMOCoreQuestModule;
import net.Indyuce.mmocore.quest.provided.NoneQuestModule;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;

public enum QuestModuleType {
    MMOCORE("MMOCore", MMOCoreQuestModule::new),
    NONE("MMOCore", NoneQuestModule::new),
    BEAUTYQUESTS("BeautyQuests", BeautyQuestModule::new),
    //BETONQUEST("BetonQuest", BetonQuestModule::new),
    QUESTCREATOR("QuestCreator", QuestCreatorModule::new),
    QUESTS("Quests", QuestsModule::new),
    //TYPEWRITER("Typewriter", TypewriterModule::new),
    ;

    private final String pluginName;
    private final Provider<QuestModule> provider;

    QuestModuleType(String pluginName, Provider<QuestModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    @NotNull
    public String getPluginName() {
        return pluginName;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public QuestModule provideModule() {
        return provider.get();
    }
}
