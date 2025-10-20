package net.Indyuce.mmocore.comp.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.event.ProfileCreateEvent;
import fr.phoenixdevt.profiles.event.ProfileRemoveEvent;
import fr.phoenixdevt.profiles.event.ProfileSelectEvent;
import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ForceClassProfileDataModule implements ProfileDataModule {
    private final NamespacedKey key;

    public ForceClassProfileDataModule() {
        final var registration = Bukkit.getServicesManager().getRegistration(ProfileProvider.class);
        Validate.notNull(registration, "Could not find ProfileAPI registration provider");
        final var profileProvider = registration.getProvider();
        profileProvider.registerModule(this);

        this.key = new NamespacedKey(MMOCore.plugin, "force_class_select");
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

        // Proxy-based profiles
        if (MythicLib.plugin.getProfileMode() == ProfileMode.PROXY) {
            event.validate(this);
            return;
        }

        final PlayerData playerData = PlayerData.get(event.getPlayerData().getPlayer());
        InventoryManager.CLASS_SELECT.newInventory(playerData, () -> event.validate(this)).open();
    }

    /**
     * Force class before profile selection once MMOCore loaded its data
     */
    @EventHandler
    public void onDataLoad(SynchronizedDataLoadEvent event) {
        if (event.getManager().getOwningPlugin().equals(MMOCore.plugin)) {
            final PlayerData playerData = (PlayerData) event.getHolder();

            // Proxy-based profiles
            if (!event.hasProfileEvent()) {
                Validate.isTrue(MythicLib.plugin.getProfileMode() == ProfileMode.PROXY, "Listened to a data load event with no profile event attached but proxy-based profiles are disabled");
                if (playerData.getProfess().equals(MMOCore.plugin.classManager.getDefaultClass()))
                    InventoryManager.CLASS_SELECT.newInventory(playerData, () -> {
                    }).open();
                return;
            }

            final ProfileSelectEvent event1 = (ProfileSelectEvent) event.getProfileEvent();

            // Validate if necessary
            if (playerData.getProfess().equals(MMOCore.plugin.classManager.getDefaultClass()))
                InventoryManager.CLASS_SELECT.newInventory(playerData, () -> event1.validate(this)).open();
            else event1.validate(this);
        }
    }

    @EventHandler
    public void onProfileRemove(ProfileRemoveEvent event) {
        event.validate(this);
    }

    @EventHandler
    public void onProfileUnload(ProfileUnloadEvent event) {
        event.validate(this);
    }
}
