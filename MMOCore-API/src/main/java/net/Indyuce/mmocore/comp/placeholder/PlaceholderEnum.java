package net.Indyuce.mmocore.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.comp.placeholder.api.PlaceholderEntry;
import io.lumine.mythic.lib.comp.placeholder.api.PlaceholderMetadata;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.version.Attributes;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.util.Language;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public enum PlaceholderEnum implements PlaceholderEntry<PlayerData> {

    //region Placeholders

    /**
     * Mana icon
     */
    mana_icon(result -> result.playerData.getProfess().getManaDisplay().getIcon()),

    /**
     * Mana name
     */
    mana_name(result -> result.playerData.getProfess().getManaDisplay().getName()),

    /**
     * Current class level
     */
    level(0, result -> String.valueOf(result.playerData.getLevel())),

    /**
     * Skill level of a given skill
     */
    skill_level_(0, result -> {
        final var skill = MythicLib.plugin.getSkills().getHandlerOrThrow(result.params());
        return String.valueOf(result.playerData.getSkillLevel(skill));
    }),

    /**
     * Points spent in skill tree
     */
    skill_tree_points_(0, result -> String.valueOf(result.playerData.getSkillTreePoints(result.params()))),

    /**
     * Given a skill slot number (int) and parameter name, return the value of
     * that skill parameter value, from that specific skill slot.
     */
    bound_skill_parameter_(0, result -> {
        final var ids = result.params().split(":", 2);
        final var skillSlot = Integer.parseInt(ids[1]);
        final var found = result.playerData.getBoundSkill(skillSlot);
        Validate.notNull(found, "Class does not have that skill");

        final var castable = found.toCastable(result.playerData);
        final var value = result.playerData.getMMOPlayerData().getSkillModifierMap().calculateValue(castable, ids[0]);
        return MythicLib.plugin.getMMOConfig().decimal.format(value);
    }),

    /**
     * Returns a player's value of a skill parameter.
     */
    skill_parameter_(0, result -> {
        final var ids = result.params().split(":");
        final var parameterId = ids[0];
        final var skillId = ids[1];
        final var skill = MythicLib.plugin.getSkills().getHandlerOrThrow(skillId);
        final var classSkill = result.playerData.getProfess().getSkill(skill);
        Validate.notNull(classSkill, "Class does not have that skill");

        final var castable = classSkill.toCastable(result.playerData);
        final var value = result.playerData.getMMOPlayerData().getSkillModifierMap().calculateValue(castable, parameterId);
        // TODO modifier-specific numeric formatting
        return MythicLib.plugin.getMMOConfig().decimal.format(value);
    }),

    @BackwardsCompatibility(version = "1.13.1-SNAPSHOT")
    skill_modifier_(skill_parameter_),

    /**
     * Points spent in a given attribute
     */
    attribute_points_spent_(0, metadata -> {
        final var attributeInstance = metadata.playerData.getAttributes().getInstance(metadata.params());
        return String.valueOf(attributeInstance.getBase());
    }),

    /**
     * Percentage of XP towards next class level
     */
    level_percent(0, metadata -> {
        var current = metadata.playerData.getExperience();
        var next = metadata.playerData.getLevelUpExperience();
        return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
    }),

    /**
     * Class name
     */
    clazz("class", "", metadata -> metadata.playerData.getProfess().getName()),

    /**
     * Current class ID
     */
    class_id(metadata -> metadata.playerData.getProfess().getId()),

    /**
     * Current player health, using the same format as max health
     */
    health(metadata -> StatManager.format("MAX_HEALTH", metadata.playerData.getPlayer().getHealth())),

    /**
     * Maximum player health
     */
    max_health(metadata -> StatManager.format("MAX_HEALTH", metadata.playerData.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue())),

    /**
     * Built-in simple health bar with 20 segments
     */
    health_bar("", metadata -> {
        Validate.isTrue(metadata.playerData.isOnline(), "Player is offline");

        var format = new StringBuilder();
        var maxHealth = Math.max(1e-5, metadata.playerData.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue());
        var ratio = 20 * metadata.playerData.getPlayer().getHealth() / maxHealth;
        for (int j = 1; j < 20; j++)
            format.append(ratio >= j ? ChatColor.RED : ratio >= j - .5 ? ChatColor.DARK_RED : ChatColor.DARK_GRAY).append(AltChar.listSquare);
        return format.toString();
    }),

    /**
     * Percentage of XP towards next profession level
     */
    profession_percent_(0, metadata -> {
        PlayerProfessions professions = metadata.playerData.getCollectionSkills();
        String name = metadata.params().replace(" ", "-").replace("_", "-").toLowerCase();
        Profession profession = MMOCore.plugin.professionManager.get(name);
        double current = professions.getExperience(profession), next = professions.getLevelUpExperience(profession);
        return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
    }),

    /**
     * Is the player currently in casting mode
     */
    is_casting(false, metadata -> String.valueOf(metadata.playerData.isCasting())),

    /**
     * Is the player currently in combat
     */
    in_combat(false, metadata -> String.valueOf(metadata.playerData.isInCombat())),

    /**
     * Is the player currently in PvP mode
     */
    pvp_mode(false, metadata -> String.valueOf(metadata.playerData.getCombat().isInPvpMode())),

    /**
     * Time since player is in combat, in seconds, formatted
     */
    since_enter_combat(metadata -> {
        if (!metadata.playerData.isInCombat()) return "-1";
        return MythicLib.plugin.getMMOConfig().decimal.format((System.currentTimeMillis() - metadata.playerData.getCombat().getLastEntry()) / 1000.);
    }),

    /**
     * Time left of invulnerability, for combat, in seconds, formatted
     */
    invulnerability_left(0, metadata -> MythicLib.plugin.getMMOConfig().decimal.format(Math.max(0, (metadata.playerData.getCombat().getInvulnerableTill() - System.currentTimeMillis()) / 1000.))),

    /**
     * Time since last hit taken/given, in seconds, formatted
     */
    since_last_hit(0, metadata -> {
        if (!metadata.playerData.isInCombat()) return "-1";
        return MythicLib.plugin.getMMOConfig().decimal.format((System.currentTimeMillis() - metadata.playerData.getCombat().getLastHit()) / 1000.);
    }),

    /**
     * ID of the skill bound to a given slot
     */
    id_bound_(metadata -> {
        final int slot = Math.max(1, Integer.parseInt(metadata.params()));
        final ClassSkill info = metadata.playerData.getBoundSkill(slot);
        Validate.notNull(info, "No skill bound");
        return info.getSkill().getId();
    }),

    /**
     * Returns the key that needs to be pressed to cast slot in slot N
     */
    cast_slot_offset_(0, metadata -> {
        final var online = metadata.playerData.getPlayer();
        Validate.notNull(online, "Player is offline");
        final var query = Integer.parseInt(metadata.params());

        final var bound = metadata.playerData.getBoundSkills().get(query);
        if (bound == null || bound.isPassive()) return String.valueOf(0);

        int slot = bound.skillBarCastSlot;
        // Offset due to player's hotbar location
        if (online.getInventory().getHeldItemSlot() < slot) slot++;
        return String.valueOf(slot);
    }),

    /**
     * Is there a passive skill bound to given slot
     */
    passive_bound_(false, metadata -> {
        final var slot = Integer.parseInt(metadata.params());
        final var skill = metadata.playerData.getBoundSkill(slot);
        return String.valueOf(skill != null && skill.getTrigger().isPassive());
    }),

    /**
     * Name of the skill bound to a given slot, in green if not on cooldown, red if on cooldown
     */
    bound_(metadata -> {
        final int slot = Math.max(1, Integer.parseInt(metadata.params()));
        final ClassSkill skill = metadata.playerData.getBoundSkill(slot);
        if (skill == null) return Language.NO_SKILL_PLACEHOLDER.getFormat();
        return (metadata.playerData.getCooldownMap().isOnCooldown(skill) ? ChatColor.RED : ChatColor.GREEN) + skill.getSkill().getName();
    }),

    /**
     * Cooldown left of the skill bound to a given slot, in seconds, formatted
     */
    cooldown_bound_(0, metadata -> {
        int slot = Math.max(0, Integer.parseInt(metadata.params()));
        if (metadata.playerData.hasSkillBound(slot))
            return Double.toString(metadata.playerData.getCooldownMap().getCooldown(metadata.playerData.getBoundSkill(slot)));
        else return Language.NO_SKILL_PLACEHOLDER.getFormat();
    }),

    /**
     * Experience in a given profession
     */
    profession_experience_(0, metadata -> {
        final var profId = metadata.params().replace(" ", "-").replace("_", "-").toLowerCase();
        final var exp = metadata.playerData.getCollectionSkills().getExperience(profId);
        return MythicLib.plugin.getMMOConfig().decimal.format(exp);
    }),

    /**
     * Exp needed for next level in a given profession
     */
    profession_next_level_(0, metadata -> {
        final var professionId = metadata.params().replace(" ", "-").replace("_", "-").toLowerCase();
        final var profession = MMOCore.plugin.professionManager.get(professionId);
        Validate.notNull(profession, "Profession not found");
        final var professionLevel = metadata.playerData.getCollectionSkills().getLevel(profession);
        return String.valueOf(profession.getExpCurve().getExperience(metadata.playerData, professionLevel));
    }),

    /**
     * Number of members in the player's party
     */
    party_count(0, metadata -> {
        final @Nullable AbstractParty party = metadata.playerData.getParty();
        return party == null ? String.valueOf(0) : String.valueOf(party.countMembers());
    }),

    /**
     * Name of n-th party member
     */
    party_member_("", metadata -> {
        final int n = Integer.parseInt(metadata.params()) - 1;
        final @Nullable AbstractParty party = metadata.playerData.getParty();
        if (party == null) return "";
        if (n >= party.countMembers()) return "";
        final @Nullable PlayerData member = party.getMember(n);
        if (member == null) return "";
        return member.getPlayer().getName();
    }),

    /**
     * Number of online friends
     */
    online_friends(0, metadata -> {
        int count = 0;
        for (UUID friendId : metadata.playerData.getFriends()) if (Bukkit.getPlayer(friendId) != null) count++;
        return String.valueOf(count);
    }),

    /**
     * Name of n-th online friend
     */
    online_friend_(metadata -> {
        final int n = Integer.parseInt(metadata.params()) - 1;
        if (n >= metadata.playerData.getFriends().size()) return "";
        final @Nullable Player friend = Bukkit.getPlayer(metadata.playerData.getFriends().get(n));
        if (friend == null) return "";
        return friend.getName();
    }),

    /**
     * Profession current level
     */
    profession_(0, metadata -> {
        final var professionId = UtilityMethods.kebabCase(metadata.params());
        final @Nullable var profession = MMOCore.plugin.professionManager.get(professionId);
        Validate.notNull(profession, "Profession not found");
        return String.valueOf(metadata.playerData.getCollectionSkills().getLevel(profession));
    }),

    /**
     * Player total experience
     */
    experience(0, metadata -> MythicLib.plugin.getMMOConfig().decimal.format(metadata.playerData.getExperience())),

    /**
     * Experience needed for next level
     */
    next_level(0, metadata -> String.valueOf(metadata.playerData.getLevelUpExperience())),

    /**
     * Class points
     */
    class_points(0, metadata -> String.valueOf(metadata.playerData.getClassPoints())),

    /**
     * Skill points
     */
    skill_points(0, metadata -> String.valueOf(metadata.playerData.getSkillPoints())),

    /**
     * Attribute points
     */
    attribute_points(0, metadata -> String.valueOf(metadata.playerData.getAttributePoints())),

    /**
     * Attribute reallocation points
     */
    attribute_reallocation_points(0, metadata -> String.valueOf(metadata.playerData.getAttributeReallocationPoints())),

    /**
     * Total attribute value, including all points spent and modifiers
     */
    attribute_(0, metadata -> {
        final var attributeId = metadata.params().toLowerCase().replace("_", "-");
        return String.valueOf(metadata.playerData.getAttributes().getAttribute(attributeId));
    }),

    /**
     * Current mana
     */
    mana(0, metadata -> MythicLib.plugin.getMMOConfig().decimal.format(metadata.playerData.getMana())),

    /**
     * Formatted mana bar
     */
    mana_bar(metadata -> metadata.playerData.getProfess().getManaDisplay().generateBar(metadata.playerData.getMana(), metadata.playerData.getStats().getStat("MAX_MANA"))),

    /**
     * Exp multiplier in a specific profession/main class
     */
    exp_multiplier_(metadata -> {
        final var format = metadata.params().toLowerCase().replace("_", "-").replace(" ", "-");
        final var profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
        return MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.boosterManager.getMultiplier(profession) * 100);
    }),

    /**
     * Exp boost (multiplier - 1) in a specific profession/main class
     */
    exp_boost_(metadata -> {
        String format = metadata.params().toLowerCase().replace("_", "-").replace(" ", "-");
        Profession profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
        return MythicLib.plugin.getMMOConfig().decimal.format((MMOCore.plugin.boosterManager.getMultiplier(profession) - 1) * 100);
    }),

    /**
     * Current stamina
     */
    stamina(0, metadata -> MythicLib.plugin.getMMOConfig().decimal.format(metadata.playerData.getStamina())),

    /**
     * Formatted stamina bar
     */
    stamina_bar(metadata -> {
        StringBuilder format = new StringBuilder();
        double ratio = 20 * metadata.playerData.getStamina() / metadata.playerData.getStats().getStat("MAX_STAMINA");
        for (double j = 1; j < 20; j++)
            format.append(ratio >= j ? MMOCore.plugin.configManager.staminaFull
                            : ratio >= j - .5 ? MMOCore.plugin.configManager.staminaHalf : MMOCore.plugin.configManager.staminaEmpty)
                    .append(AltChar.listSquare);
        return format.toString();
    }),

    /**
     * Value of a given stat
     */
    stat_(0, metadata -> {
        final String stat = UtilityMethods.enumName(metadata.params());
        return StatManager.format(stat, metadata.playerData.getMMOPlayerData());
    }),

    /**
     * Current stellium
     */
    stellium(0, metadata -> MythicLib.plugin.getMMOConfig().decimal.format(metadata.playerData.getStellium())),

    /**
     * Formatted stellium bar
     */
    stellium_bar(metadata -> {
        StringBuilder format = new StringBuilder();
        double ratio = 20 * metadata.playerData.getStellium() / metadata.playerData.getStats().getStat("MAX_STELLIUM");
        for (double j = 1; j < 20; j++)
            format.append(ratio >= j ? ChatColor.BLUE : ratio >= j - .5 ? ChatColor.AQUA : ChatColor.WHITE).append(AltChar.listSquare);
        return format.toString();
    }),

    /**
     * Current quest name
     */
    quest(metadata -> {
        PlayerQuests data = metadata.playerData.getQuestData();
        return data.hasCurrent() ? data.getCurrent().getQuest().getName() : "";
    }),

    /**
     * Current quest progress in percentage
     */
    quest_progress(metadata -> {
        PlayerQuests data = metadata.playerData.getQuestData();
        if (!data.hasCurrent()) return String.valueOf(0);
        return MythicLib.plugin.getMMOConfig().decimal.format((double) data.getCurrent().getObjectiveNumber() / data.getCurrent().getQuest().getObjectives().size() * 100L);
    }),

    /**
     * Current quest objective formatted lore
     */
    quest_objective(metadata -> {
        PlayerQuests data = metadata.playerData.getQuestData();
        return data.hasCurrent() ? data.getCurrent().getFormattedLore() : "";
    }),

    guild_name(metadata -> {
        if (metadata.playerData.getGuild() == null) return "";
        return metadata.playerData.getGuild().getName();
    }),

    guild_tag(metadata -> {
        if (metadata.playerData.getGuild() == null) return "";
        return metadata.playerData.getGuild().getTag();
    }),

    guild_leader(metadata -> {
        if (metadata.playerData.getGuild() == null) return "";
        return Bukkit.getOfflinePlayer(metadata.playerData.getGuild().getOwner()).getName();
    }),

    @BackwardsCompatibility(version = "1.13.1-SNAPSHOT")
    @Deprecated(forRemoval = true)
    leader(guild_leader),

    guild_members(0, metadata -> {
        if (metadata.playerData.getGuild() == null) return String.valueOf(0);
        return String.valueOf(metadata.playerData.getGuild().countMembers());
    }),

    guild_online_members(0, metadata -> {
        if (metadata.playerData.getGuild() == null) return String.valueOf(0);
        return String.valueOf(metadata.playerData.getGuild().countOnlineMembers());
    }),

    //endregion

    ;

    private final String prefix, fallback;
    private final Function<PlaceholderMetadata<PlayerData>, String> parser;

    private static final String DEFAULT_FALLBACK = "";

    PlaceholderEnum(PlaceholderEnum delegate) {
        this(delegate.fallback, delegate.parser);
    }

    PlaceholderEnum(Function<PlaceholderMetadata<PlayerData>, String> parser) {
        this(DEFAULT_FALLBACK, parser);
    }

    PlaceholderEnum(Object fallback, Function<PlaceholderMetadata<PlayerData>, String> parser) {
        this.prefix = name();
        this.parser = parser;
        this.fallback = String.valueOf(Objects.requireNonNull(fallback, "Default value cannot be null"));
    }

    PlaceholderEnum(String prefix, Object fallback, Function<PlaceholderMetadata<PlayerData>, String> parser) {
        this.prefix = prefix;
        this.parser = parser;
        this.fallback = String.valueOf(Objects.requireNonNull(fallback, "Default value cannot be null"));
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getFallback() {
        return fallback;
    }

    @Override
    public @NotNull String parse(@NotNull PlaceholderMetadata<PlayerData> placeholderMetadata) {
        return this.parser.apply(placeholderMetadata);
    }
}
