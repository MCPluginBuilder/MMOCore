package net.Indyuce.mmocore.comp;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import io.lumine.mythic.lib.rpg.ManaModule;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.jetbrains.annotations.NotNull;

// Used in MythicLib, do not change class path.
public class MMOCoreModule implements LevelModule, ClassModule, ManaModule {

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData mmoPlayerData) {
        return PlayerData.get(mmoPlayerData.getUniqueId()).getProfess().getName();
    }

    @Override
    public int getLevel(@NotNull MMOPlayerData mmoPlayerData) {
        return PlayerData.get(mmoPlayerData.getUniqueId()).getLevel();
    }

    @Override
    public double getMana(@NotNull MMOPlayerData mmoPlayerData) {
        return PlayerData.get(mmoPlayerData.getUniqueId()).getMana();
    }

    @Override
    public double getStamina(@NotNull MMOPlayerData mmoPlayerData) {
        return PlayerData.get(mmoPlayerData.getUniqueId()).getStamina();
    }

    @Override
    public boolean setMana(@NotNull MMOPlayerData mmoPlayerData, double v, @NotNull ResourceUpdateReason resourceUpdateReason) {
        PlayerData.get(mmoPlayerData.getUniqueId()).setMana(v, resourceUpdateReason);
        return true;
    }

    @Override
    public boolean setStamina(@NotNull MMOPlayerData mmoPlayerData, double v, @NotNull ResourceUpdateReason resourceUpdateReason) {
        PlayerData.get(mmoPlayerData.getUniqueId()).setStamina(v, resourceUpdateReason);
        return true;
    }
}
