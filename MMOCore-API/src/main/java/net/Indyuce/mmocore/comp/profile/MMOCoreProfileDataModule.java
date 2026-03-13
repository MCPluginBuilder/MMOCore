package net.Indyuce.mmocore.comp.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.placeholder.PlaceholderProcessor;
import fr.phoenixdevt.profiles.placeholder.PlaceholderRequest;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.profile.DefaultProfileDataModule;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import org.jetbrains.annotations.NotNull;

public class MMOCoreProfileDataModule extends DefaultProfileDataModule implements PlaceholderProcessor {
    public MMOCoreProfileDataModule(@NotNull PlayerDataManager playerDataManager) {
        super(playerDataManager);
    }

    @Override
    public ProfileDataModule getDataModule() {
        return this;
    }

    @Override
    public @NotNull String getPlaceholderPrefix() {
        return "mmocore";
    }

    @Override
    public void processPlaceholderRequest(PlaceholderRequest placeholderRequest) {
        var lookupData = new PlayerData(new MMOPlayerData(true, placeholderRequest.getProfile().getUniqueId()));
        MMOCore.plugin.playerDataManager.loadData(lookupData).thenRun(() -> {

            // Very likely to be in an async thread
            // Catch errors and print to console
            try {
                placeholderRequest.addPlaceholder("class", lookupData.getProfess().getName());
                placeholderRequest.addPlaceholder("level", lookupData.getLevel());

                for (var attribute : MMOCore.plugin.attributeManager.getAll())
                    placeholderRequest.addPlaceholder("attribute_" + attribute.getId().replace("-", "_"), lookupData.getAttributes().getInstance(attribute).getBase());

                for (var profession : MMOCore.plugin.professionManager.getAll())
                    placeholderRequest.addPlaceholder("profession_" + profession.getId().replace("-", "_"), lookupData.getCollectionSkills().getLevel(profession));

                placeholderRequest.addPlaceholder("exp", MythicLib.plugin.getMMOConfig().decimal.format(lookupData.getExperience()));
                placeholderRequest.addPlaceholder("exp_next_level", lookupData.getLevelUpExperience());
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // Validate placeholders anyways
            finally {
                placeholderRequest.validate();
            }
        });
    }
}
