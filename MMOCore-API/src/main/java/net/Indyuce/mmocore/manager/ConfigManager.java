package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.config.ConfigVersioner;
import io.lumine.mythic.lib.util.config.YamlFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.player.Message;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    public final boolean overrideVanillaExp, canCreativeCast, passiveSkillsNeedBinding, cobbleGeneratorXP, saveDefaultClassInfo,
            splitMainExp, splitProfessionExp, disableQuestBossBar, pvpModeEnabled, pvpModeInvulnerabilityCanDamage, forceClassSelection,
            enableGlobalSkillTreeGUI, enableSpecificSkillTreeGUI, waypointAutoPathCalculation, waypointLinkReciprocity,
            waypointHideLocked, shareExp, shareSkillPts, shareAttributePts, shareSkillReallocPts, shareAttributeReallocPts;
    public final ChatColor staminaFull, staminaHalf, staminaEmpty;
    public final long combatLogTimer, lootChestExpireTime, lootChestPlayerCooldown, globalSkillCooldown;
    public final double lootChestsChanceWeight, dropItemsChanceWeight, fishingDropsChanceWeight, partyMaxExpSplitRange, pvpModeToggleOnCooldown, pvpModeToggleOffCooldown, pvpModeCombatCooldown,
            pvpModeCombatTimeout, pvpModeInvulnerabilityTimeRegionChange, pvpModeInvulnerabilityTimeCommand, pvpModeRegionEnterCooldown, pvpModeRegionLeaveCooldown;
    public final int maxPartyLevelDifference, maxPartyPlayers, minCombatLevel, maxCombatLevelDifference, skillTreeScrollStepX, skillTreeScrollStepY, waypointWarpTime;
    public final List<EntityDamageEvent.DamageCause> combatLogDamageCauses = new ArrayList<>();
    public final String partyChatPrefix;

    private static final List<Runnable> CONFIG_UPDATES = ConfigVersioner.nops(9, ConfigManager::fixMMOCoreCommand);

    /*
     * The instance must be created after the other managers since all it does
     * is to update them based on the config except for the classes which are
     * already loaded based on the config
     */
    public ConfigManager() {

        ConfigVersioner.applyConfigVersioner(MMOCore.plugin, CONFIG_UPDATES);

        // Backwards compatibility for older configs
        {
            FileUtils.moveIfExists(MMOCore.plugin, "attributes.yml", "attributes");
            FileUtils.moveIfExists(MMOCore.plugin, "exp-tables.yml", "exp-tables");
            FileUtils.moveIfExists(MMOCore.plugin, "loot-chests.yml", "loot-chests");
            FileUtils.moveIfExists(MMOCore.plugin, "waypoints.yml", "waypoints");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "attributes").exists()) {
            copyDefaultFile("attributes/default_attributes.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "classes").exists()) {
            copyDefaultFile("classes/mage/arcane-mage.yml");
            copyDefaultFile("classes/mage/mage.yml");
            copyDefaultFile("classes/human.yml");
            copyDefaultFile("classes/marksman.yml");
            copyDefaultFile("classes/paladin.yml");
            copyDefaultFile("classes/rogue.yml");
            copyDefaultFile("classes/warrior.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "drop-tables").exists())
            copyDefaultFile("drop-tables/example_drop_tables.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "exp-tables").exists())
            copyDefaultFile("exp-tables/default_exp_tables.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "loot-chests").exists())
            copyDefaultFile("loot-chests/default_loot_chests.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "professions").exists()) {
            copyDefaultFile("professions/alchemy.yml");
            copyDefaultFile("professions/farming.yml");
            copyDefaultFile("professions/fishing.yml");
            copyDefaultFile("professions/mining.yml");
            copyDefaultFile("professions/smelting.yml");
            copyDefaultFile("professions/smithing.yml");
            copyDefaultFile("professions/woodcutting.yml");
            copyDefaultFile("professions/enchanting.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "quests").exists()) {
            copyDefaultFile("quests/adv-begins.yml");
            copyDefaultFile("quests/tutorial.yml");
            copyDefaultFile("quests/fetch-mango.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "skill-trees").exists()) {
            copyDefaultFile("skill-trees/combat.yml");
            copyDefaultFile("skill-trees/mage-arcane-mage.yml");
            copyDefaultFile("skill-trees/rogue-marksman.yml");
            copyDefaultFile("skill-trees/warrior-paladin.yml");
            copyDefaultFile("skill-trees/general.yml");
            copyDefaultFile("skill-trees/loop.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "waypoints").exists()) {
            copyDefaultFile("waypoints/default_waypoints.yml");
        }

        copyDefaultFile("conditions.yml");
        copyDefaultFile("exp-sources.yml");
        copyDefaultFile("guilds.yml");
        copyDefaultFile("items.yml");
        copyDefaultFile("messages.yml");
        copyDefaultFile("restrictions.yml");
        copyDefaultFile("stats.yml");

        // Reload messages
        Message.loadMessagesFromConfig();

        ////////////////
        // Without default values
        ////////////////
        {
            final var config = new YamlFile(MMOCore.plugin, "config").getContent();

            // Skill casting
            if (config.contains("skill-casting")) try {
                final var castingMode = SkillCastingMode.valueOf(UtilityMethods.enumName(config.getString("skill-casting.mode")));
                castingMode.setCurrent(config.getConfigurationSection("skill-casting"));
            } catch (RuntimeException exception) {
                MMOCore.log(Level.WARNING, "Could not load skill casting: " + exception.getMessage());
            }
            else SkillCastingMode.NONE.setCurrent(new YamlConfiguration());
        }

        ////////////////
        // With default values
        ////////////////
        {
            final var config = MMOCore.plugin.getConfig();

            // Combat log
            combatLogTimer = config.getInt("combat-log.timer") * 20L;
            combatLogDamageCauses.clear();
            for (var key : config.getStringList("combat-log.causes"))
                try {
                    combatLogDamageCauses.add(EntityDamageEvent.DamageCause.valueOf(UtilityMethods.enumName(key)));
                } catch (Exception exception) {
                    MMOCore.log(Level.WARNING, "Could not find damage cause called '" + key + "'");
                }
            enableGlobalSkillTreeGUI = config.getBoolean("enable-global-skill-tree-gui");
            enableSpecificSkillTreeGUI = config.getBoolean("enable-specific-skill-tree-gui");
            lootChestExpireTime = Math.max(config.getInt("loot-chests.chest-expire-time"), 1) * 20;
            lootChestPlayerCooldown = (long) config.getDouble("player-cooldown") * 1000L;
            globalSkillCooldown = config.getLong("global-skill-cooldown") * 50;
            lootChestsChanceWeight = config.getDouble("chance-stat-weight.loot-chests");
            dropItemsChanceWeight = config.getDouble("chance-stat-weight.drop-items");
            fishingDropsChanceWeight = config.getDouble("chance-stat-weight.fishing-drops");
            maxPartyLevelDifference = config.getInt("party.max-level-difference");
            partyMaxExpSplitRange = config.getDouble("party.max-exp-split-range");
            splitMainExp = config.getBoolean("party.main-exp-split");
            splitProfessionExp = config.getBoolean("party.profession-exp-split");
            disableQuestBossBar = config.getBoolean("mmocore-quests.disable-boss-bar");
            forceClassSelection = config.getBoolean("force-class-selection");
            waypointWarpTime = config.getInt("waypoints.default-warp-time");
            waypointAutoPathCalculation = config.getBoolean("waypoints.auto_path_calculation");
            waypointLinkReciprocity = config.getBoolean("waypoints.link_reciprocity");
            maxPartyPlayers = Math.max(2, config.getInt("party.max-players", 8));
            partyChatPrefix = config.getString("party.chat-prefix");

            // Combat
            pvpModeEnabled = config.getBoolean("pvp_mode.enabled");
            pvpModeToggleOnCooldown = config.getDouble("pvp_mode.cooldown.toggle_on");
            pvpModeToggleOffCooldown = config.getDouble("pvp_mode.cooldown.toggle_off");
            pvpModeCombatCooldown = config.getDouble("pvp_mode.cooldown.combat");
            pvpModeRegionEnterCooldown = config.getDouble("pvp_mode.cooldown.region_enter");
            pvpModeRegionLeaveCooldown = config.getDouble("pvp_mode.cooldown.region_leave");
            pvpModeCombatTimeout = config.getDouble("pvp_mode.combat_timeout");
            pvpModeInvulnerabilityTimeCommand = config.getDouble("pvp_mode.invulnerability.time.command");
            pvpModeInvulnerabilityTimeRegionChange = config.getDouble("pvp_mode.invulnerability.time.region_change");
            pvpModeInvulnerabilityCanDamage = config.getBoolean("pvp_mode.invulnerability.can_damage");
            minCombatLevel = config.getInt("pvp_mode.min_level");
            maxCombatLevelDifference = config.getInt("pvp_mode.max_level_difference");
            skillTreeScrollStepX = config.getInt("skill-tree-scroll-step-x", 1);
            skillTreeScrollStepY = config.getInt("skill-tree-scroll-step-y", 1);
            // Resources
            staminaFull = getColorOrDefault(config, "stamina-whole", ChatColor.GREEN);
            staminaHalf = getColorOrDefault(config, "stamina-half", ChatColor.DARK_GRAY);
            staminaEmpty = getColorOrDefault(config, "stamina-empty", ChatColor.WHITE);

            passiveSkillsNeedBinding = config.getBoolean("passive-skill-need-bound");
            canCreativeCast = config.getBoolean("can-creative-cast");
            cobbleGeneratorXP = config.getBoolean("should-cobblestone-generators-give-exp");
            saveDefaultClassInfo = config.getBoolean("save-default-class-info");
            overrideVanillaExp = config.getBoolean("override-vanilla-exp");

            // Data share across classes
            shareExp = config.getBoolean("share_across_classes.experience");
            shareSkillPts = config.getBoolean("share_across_classes.skill_points");
            shareAttributePts = config.getBoolean("share_across_classes.attribute_points");
            shareSkillReallocPts = config.getBoolean("share_across_classes.skill_reallocation_points");
            shareAttributeReallocPts = config.getBoolean("share_across_classes.attribute_reallocation_points");
        }
    }

    @BackwardsCompatibility(version = "1.13.1-SNAPSHOT")
    private static void fixMMOCoreCommand() {
        final var commands = new YamlFile(MMOCore.plugin, "commands");
        commands.getContent().set("mmocore.verbose", "ALL");
        commands.save();
    }

    @NotNull
    private ChatColor getColorOrDefault(ConfigurationSection config, String key, ChatColor defaultColor) {
        try {
            return ChatColor.valueOf(config.getString("resource-bar-colors." + key, "").toUpperCase());
        } catch (Exception exception) {
            MMOCore.log(Level.WARNING, "Could not read resource bar color from '" + key + "': using default.");
            return defaultColor;
        }
    }

    public void copyDefaultFile(String path) {
        FileUtils.copyDefaultFile(MMOCore.plugin, path);
    }

    //region Deprecated

    @Deprecated
    public PlayerInput newPlayerInput(Player player, InputType type, Consumer<String> output) {
        return new ChatInput(player, type, null, output);
    }

    @Deprecated
    public void loadDefaultFile(String name) {
        copyDefaultFile(name);
    }

    @Deprecated
    public void copyDefaultFile(String path, String name) {
        if (path.isEmpty()) copyDefaultFile(name);
        else copyDefaultFile(path + "/" + name);
    }

    /**
     * @see net.Indyuce.mmocore.player.Message
     * @deprecated
     */
    @Deprecated
    public List<String> getMessage(String key) {
        final var messages = new YamlFile(MMOCore.plugin, "messages").getContent();
        return messages.getStringList(key);
    }

    /**
     * @return The original object, which should be cloned afterwards!!
     * @see net.Indyuce.mmocore.player.Message
     * @deprecated
     */
    @Nullable
    @Deprecated
    public Object getMessageObject(String key) {
        final var messages = new YamlFile(MMOCore.plugin, "messages").getContent();
        return messages.get(key);
    }

    /**
     * @see net.Indyuce.mmocore.player.Message
     * @deprecated
     */
    @Deprecated
    public SimpleMessage getSimpleMessage(String key, String... placeholders) {
        SimpleMessage wrapper = new SimpleMessage(ConfigMessage.fromKey(key));
        wrapper.message.addPlaceholders(placeholders);
        return wrapper;
    }

    @Deprecated
    public static class SimpleMessage {
        private final ConfigMessage message;

        @Deprecated
        public SimpleMessage(ConfigMessage message) {
            this.message = message;
        }

        @Deprecated
        public String message() {
            return message.getLines().isEmpty() ? "" : message.getLines().get(0);
        }

        @Deprecated
        public boolean send(Player player) {
            message.send(player);
            return !message.getLines().isEmpty();
        }
    }

    //endregion
}
