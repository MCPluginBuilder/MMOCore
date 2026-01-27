package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.util.config.YamlFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.builtin.*;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Commands which can be disabled using commands.yml
 */
public enum ToggleableCommand {
    CAST("cast", "mmocore.cast", "Enter casting mode", CastCommand::new),
    PLAYER("player", "mmocore.profile", "Displays player stats", PlayerStatsCommand::new, "p", "profile"),
    ATTRIBUTES("attributes", "mmocore.attributes", "Display and manage attributes", AttributesCommand::new, "att", "stats"),
    CLASS("class", "mmocore.class-select", "Select a new class", ClassCommand::new, "c"),
    WAYPOINTS("waypoints", "mmocore.waypoints", "Display discovered waypoints", WaypointsCommand::new, "wp"),
    QUESTS("quests", "mmocore.quests", "Display available quests", QuestsCommand::new, "q", "journal"),
    SKILLS("skills", "mmocore.skills", "Spend skill points to unlock new skills", SkillsCommand::new, "s"),
    FRIENDS("friends", "mmocore.friends", "Show online/offline friends", FriendsCommand::new, "f"),
    PARTY("party", "mmocore.party", "Invite players in a party to split exp", PartyCommand::new, () -> (MMOCore.plugin.partyModule instanceof MMOCorePartyModule)),
    GUILD("guild", "mmocore.guild", "Show players in current guild", GuildCommand::new),
    WITHDRAW("withdraw", "mmocore.currency", "Withdraw money into coins and notes", WithdrawCommand::new, () -> MMOCore.plugin.hasEconomy() && MMOCore.plugin.economy.isValid(), "w"),
    SKILL_TREES("skilltrees", "mmocore.skilltrees", "Open up the skill tree menu", SkillTreesCommand::new, "st", "trees", "tree"),
    DEPOSIT("deposit", "mmocore.currency", "Open the currency deposit menu", DepositCommand::new, "d"),
    PVP_MODE("pvpmode", "mmocore.pvpmode", "Toggle on/off PVP mode.", PvpModeCommand::new, "pvp");

    private final String mainLabel;
    private final String description, permission;
    private final Function<ConfigurationSection, CommandTreeRoot> generator;
    private final List<String> aliases;
    private final Supplier<Boolean> enabled;

    ToggleableCommand(@NotNull String mainLabel, @Nullable String permission, @NotNull String description, @NotNull Function<ConfigurationSection, CommandTreeRoot> generator, @NotNull String... aliases) {
        this(mainLabel, permission, description, generator, null, aliases);
    }

    ToggleableCommand(@NotNull String mainLabel, @Nullable String permission, @NotNull String description, @NotNull Function<ConfigurationSection, CommandTreeRoot> generator, @Nullable Supplier<Boolean> enabled, @NotNull String... aliases) {
        this.mainLabel = mainLabel;
        this.permission = permission;
        this.description = description;
        this.generator = generator;
        this.aliases = Arrays.asList(aliases);
        this.enabled = enabled == null ? () -> true : enabled;
    }

    public String getMainLabel() {
        return mainLabel;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getConfigPath() {
        return name().toLowerCase().replace("_", "-");
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    @Deprecated
    public static void register() {
        loadCommands();
    }

    public static void loadCommands() {

        // Load default config file
        final var config = new YamlFile(MMOCore.plugin, "commands");
        if (!config.exists()) {
            for (ToggleableCommand cmd : values()) {
                final String path = cmd.getConfigPath();
                config.getContent().set(path + ".main", cmd.mainLabel);
                config.getContent().set(path + ".aliases", cmd.aliases);
                config.getContent().set(path + ".description", cmd.description);
                config.getContent().set(path + ".permission", cmd.permission);
                config.getContent().set(path + ".verbose", "ALL");
            }

            config.save();
        }

        // Enable commands individually
        final var commandMap = UtilityMethods.getCommandMap();
        for (var cmd : values())
            if (cmd.enabled.get() && config.getContent().contains(cmd.getConfigPath()))
                commandMap.register("mmocore", cmd.generator.apply(config.getContent().getConfigurationSection(cmd.getConfigPath())).toBukkit());
    }
}
