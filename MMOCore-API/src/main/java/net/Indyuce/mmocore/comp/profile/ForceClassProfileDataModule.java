package net.Indyuce.mmocore.comp.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.event.ProfileCreateEvent;
import fr.phoenixdevt.profiles.event.ProfileRemoveEvent;
import fr.phoenixdevt.profiles.event.ProfileSelectEvent;
import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ForceClassProfileDataModule implements ProfileDataModule {
    private final NamespacedKey key;

    public ForceClassProfileDataModule() {
        this.key = new NamespacedKey(MMOCore.plugin, "force_class_select");

        final var registration = Bukkit.getServicesManager().getRegistration(ProfileProvider.class);
        Validate.notNull(registration, "Could not find ProfileAPI registration provider");
        final var profileProvider = registration.getProvider();
        profileProvider.registerModule(this);
    }

    @Override
    public @NotNull JavaPlugin getOwningPlugin() {
        return MMOCore.plugin;
    }

    @Override
    public @NotNull NamespacedKey getId() {
        return this.key;
    }

    /**
     * Force class before profile creation
     */
    @EventHandler
    public void onProfileCreate(ProfileCreateEvent event) {
        final var playerData = PlayerData.get(event.getPlayerData().getPlayer());

        // Will be prompted on profile application in proxy-mode
        if (MythicLib.plugin.getProfileMode() == ProfileMode.PROXY) {
            event.validate(this);
            return;
        }

        InventoryManager.CLASS_SELECT.newInventory(playerData, () -> event.validate(this)).open();
    }

    @EventHandler
    public void onProfileApply(ProfileSelectEvent event) {
        final var playerData = PlayerData.get(event.getPlayerData().getPlayer());
        playerData.bufferForceClassSelection(() -> {
            if (playerData.getProfess().hasOption(ClassOption.DEFAULT))
                InventoryManager.CLASS_SELECT.newInventory(playerData, () -> {
                    playerData.hasChosenClass = true;
                    event.validate(this);
                }).open();
            else {
                playerData.hasChosenClass = true;
                event.validate(this);
            }
        });
    }

    @EventHandler
    public void onProfileRemove(ProfileRemoveEvent event) {
        event.validate(this);
    }

    @EventHandler
    public void onProfileUnload(ProfileUnloadEvent event) {
        // TODO improve code
        final var playerData = PlayerData.get(event.getPlayerData().getPlayer());
        if (playerData.hasChosenClass) event.validate(this);
    }
}
