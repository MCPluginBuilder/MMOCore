package net.Indyuce.mmocore.skill.cast;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class SkillCastingHandler implements Listener {
    private final int timeoutDelay;
    private final boolean timeOutEnabled;

    public SkillCastingHandler() {
        this.timeoutDelay = 0;
        this.timeOutEnabled = false;
    }

    public SkillCastingHandler(@NotNull ConfigurationSection config) {
        timeOutEnabled = config.contains("time-out");
        timeoutDelay = config.getInt("time-out");
        Validate.isTrue(!timeOutEnabled || timeoutDelay > 0, "Timeout must be strictly positive or disabled");
    }

    public boolean doesTimeOut() {
        return timeOutEnabled;
    }

    public int getTimeoutDelay() {
        return timeoutDelay;
    }

    @NotNull
    public abstract SkillCastingMode getCastingMode();

    @NotNull
    public abstract SkillCastingInstance newInstance(@NotNull PlayerData player);

    public void onSkillBindChange(@NotNull PlayerData player) {
        // Nothing by default
    }
}
