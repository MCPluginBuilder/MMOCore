package net.Indyuce.mmocore.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.skill.CastableSkill;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;


public class RPGPlaceholders extends PlaceholderExpansion {
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Indyuce";
    }

    @Override
    public String getIdentifier() {
        return "mmocore";
    }

    @Override
    public String getVersion() {
        return MMOCore.plugin.getDescription().getVersion();
    }

    private static final String ERROR_PLACEHOLDER = "InternalError";
    private static final String NO_MATCH_PLACEHOLDER = "NoMatch";

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        final PlayerData playerData;

        try {
            playerData = PlayerData.get(player);
        } catch (Exception exception) {
            MMOCore.log(Level.WARNING, "Error while parsing placeholder '" + identifier + "': Player data not found");
            return ERROR_PLACEHOLDER;
        }

        try {

            // Mana icon
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("mana_icon"))
                return playerData.getProfess().getManaDisplay().getIcon();

            // Mana name
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("mana_name"))
                return playerData.getProfess().getManaDisplay().getName();

            // Main class level
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("level"))
                return String.valueOf(playerData.getLevel());

            // Skill level
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("skill_level_")) {
                String id = identifier.substring(12);
                var skill = MythicLib.plugin.getSkills().getHandlerOrThrow(id);
                return String.valueOf(playerData.getSkillLevel(skill));
            }

            // Points spent in skill tree
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("skill_tree_points_")) {
                int length = "skill_tree_points_".length();
                String id = identifier.substring(length);
                return String.valueOf(PlayerData.get(player).getSkillTreePoints(id));
            }

            // Given a skill slot number (int) and parameter name, return the value of
            // that skill parameter value, from that specific skill slot.
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("bound_skill_parameter_")) {
                final String[] ids = identifier.substring(22).split(":");
                final String parameterId = ids[0];
                final int skillSlot = Integer.parseInt(ids[1]);
                final ClassSkill found = playerData.getBoundSkill(skillSlot);
                Validate.notNull(found, "No skill bound at slot " + skillSlot);
                final CastableSkill castable = found.toCastable(playerData);
                final double value = playerData.getMMOPlayerData().getSkillModifierMap().calculateValue(castable, parameterId);
                return MythicLib.plugin.getMMOConfig().decimal.format(value);
            }

            // Returns a player's value of a skill parameter.
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("skill_modifier_") || identifier.startsWith("skill_parameter_")) {
                final String[] ids = identifier.substring(identifier.startsWith("skill_modifier_") ? 15 : 16).split(":");
                final String parameterId = ids[0];
                final String skillId = ids[1];
                final var skill = MythicLib.plugin.getSkills().getHandlerOrThrow(skillId);
                final var classSkill = Objects.requireNonNull(playerData.getProfess().getSkill(skill), "Class " + playerData.getProfess().getName() + " does not have skill with ID '" + skillId + "'");
                final var castable = classSkill.toCastable(playerData);
                final double value = playerData.getMMOPlayerData().getSkillModifierMap().calculateValue(castable, parameterId);
                return MythicLib.plugin.getMMOConfig().decimal.format(value);
            }

            // Points spent in one attribute
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("attribute_points_spent_")) {
                final String attributeId = identifier.substring(23);
                final var attributeInstance = Objects.requireNonNull(playerData.getAttributes().getInstance(attributeId), "Could not find attribute with ID '" + attributeId + "'");
                return String.valueOf(attributeInstance.getBase());
            }

            // Percent of progression in main level
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("level_percent")) {
                double current = playerData.getExperience(), next = playerData.getLevelUpExperience();
                return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
            }

            // Health using same format as max health
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("health"))
                return StatManager.format("MAX_HEALTH", player.getPlayer().getHealth());

            // Max health
            // Redundant with "stat_max_health" bc very commonly used
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("max_health"))
                return StatManager.format("MAX_HEALTH", player.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue());

            // Health bar
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("health_bar") && player.isOnline()) {
                StringBuilder format = new StringBuilder();
                double maxHealth = Math.max(1e-5, player.getPlayer().getAttribute(Attributes.MAX_HEALTH).getValue());
                double ratio = 20 * player.getPlayer().getHealth() / maxHealth;
                for (double j = 1; j < 20; j++)
                    format.append(ratio >= j ? ChatColor.RED : ratio >= j - .5 ? ChatColor.DARK_RED : ChatColor.DARK_GRAY).append(AltChar.listSquare);
                return format.toString();
            }

            // Main class ID
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("class_id"))
                return playerData.getProfess().getId();

            // Main class name
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("class"))
                return playerData.getProfess().getName();

            // Exp progress percent due to profession
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("profession_percent_")) {
                PlayerProfessions professions = playerData.getCollectionSkills();
                String name = identifier.substring(19).replace(" ", "-").replace("_", "-").toLowerCase();
                Profession profession = MMOCore.plugin.professionManager.get(name);
                double current = professions.getExperience(profession), next = professions.getLevelUpExperience(profession);
                return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
            }

            // Is player casting?
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("is_casting"))
                return String.valueOf(playerData.isCasting());

            // Is player in combat?
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("in_combat"))
                return String.valueOf(playerData.isInCombat());

            // PvP mode
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("pvp_mode"))
                return String.valueOf(playerData.getCombat().isInPvpMode());

            // Time since entering combat, in seconds, formatted
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("since_enter_combat"))
                return playerData.isInCombat() ? MythicLib.plugin.getMMOConfig().decimal.format((System.currentTimeMillis() - playerData.getCombat().getLastEntry()) / 1000.) : "-1";

            // Time left of invulnerability, for combat, in seconds, formatted
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("invulnerability_left"))
                return MythicLib.plugin.getMMOConfig().decimal.format(Math.max(0, (playerData.getCombat().getInvulnerableTill() - System.currentTimeMillis()) / 1000.));

            // Time since last hit, in seconds, formatted
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("since_last_hit"))
                return playerData.isInCombat() ? MythicLib.plugin.getMMOConfig().decimal.format((System.currentTimeMillis() - playerData.getCombat().getLastHit()) / 1000.) : "-1";

            // Returns the bound skill ID
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("id_bound_")) {
                final int slot = Math.max(1, Integer.parseInt(identifier.substring(9)));
                final ClassSkill info = playerData.getBoundSkill(slot);
                return info == null ? "" : info.getSkill().getId();
            }

            // Returns the key that needs to be pressed to cast slot in slot N
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("cast_slot_offset_")) {
                final Player online = player.getPlayer();
                Validate.notNull(online, "Player is offline");
                final int query = Integer.parseInt(identifier.substring(17));

                BoundSkillInfo bound = playerData.getBoundSkills().get(query);
                if (bound == null || bound.isPassive()) return String.valueOf(0);

                int slot = bound.skillBarCastSlot;
                // Offset due to player's hotbar location
                if (online.getInventory().getHeldItemSlot() < slot) slot++;
                return String.valueOf(slot);
            }

            // Is there a passive skill bound to given slot
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("passive_bound_")) {
                final int slot = Integer.parseInt(identifier.substring(14));
                final ClassSkill skill = playerData.getBoundSkill(slot);
                return String.valueOf(skill != null && skill.getTrigger().isPassive());
            }

            // Returns the bound skill name
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("bound_")) {
                final int slot = Math.max(1, Integer.parseInt(identifier.substring(6)));
                final ClassSkill skill = playerData.getBoundSkill(slot);
                if (skill == null) return MMOCore.plugin.configManager.noSkillBoundPlaceholder;
                return (playerData.getCooldownMap().isOnCooldown(skill) ? ChatColor.RED : ChatColor.GREEN) + skill.getSkill().getName();
            }

            // Returns cooldown of skill bound at given slot
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("cooldown_bound_")) {
                int slot = Math.max(0, Integer.parseInt(identifier.substring(15)));
                if (playerData.hasSkillBound(slot))
                    return Double.toString(playerData.getCooldownMap().getCooldown(playerData.getBoundSkill(slot)));
                else return MMOCore.plugin.configManager.noSkillBoundPlaceholder;
            }

            // Current exp in profession
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("profession_experience_"))
                return MythicLib.plugin.getMMOConfig().decimal.format(
                        playerData.getCollectionSkills().getExperience(identifier.substring(22).replace(" ", "-").replace("_", "-").toLowerCase()));

            // Exp needed to level up profession
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("profession_next_level_")) {
                final var professionId = identifier.substring(22).replace(" ", "-").replace("_", "-").toLowerCase();
                final @Nullable var profession = MMOCore.plugin.professionManager.get(professionId);
                Validate.notNull(profession, "Profession not found with ID " + professionId);
                final var professionLevel = playerData.getCollectionSkills().getLevel(profession);
                return String.valueOf(profession.getExpCurve().getExperience(playerData, professionLevel));
            }

            // Number of online members in party
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("party_count")) {
                final @Nullable AbstractParty party = playerData.getParty();
                return party == null ? "0" : String.valueOf(party.countMembers());
            }

            // name of nth party member
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("party_member_")) {
                final int n = Integer.parseInt(identifier.substring(13)) - 1;
                final @Nullable AbstractParty party = playerData.getParty();
                if (party == null) return ERROR_PLACEHOLDER;
                if (n >= party.countMembers()) return ERROR_PLACEHOLDER;
                final @Nullable PlayerData member = party.getMember(n);
                if (member == null) return ERROR_PLACEHOLDER;
                return member.getPlayer().getName();
            }

            // Online friend count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("online_friends")) {
                int count = 0;
                for (UUID friendId : playerData.getFriends())
                    if (Bukkit.getPlayer(friendId) != null) count++;
                return String.valueOf(count);
            }

            // Name of nth online friend
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("online_friend_")) {
                final int n = Integer.parseInt(identifier.substring(14)) - 1;
                if (n >= playerData.getFriends().size()) return ERROR_PLACEHOLDER;
                final @Nullable Player friend = Bukkit.getPlayer(playerData.getFriends().get(n));
                if (friend == null) return ERROR_PLACEHOLDER;
                return friend.getName();
            }

            // Profesion level
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("profession_")) {
                final var professionId = UtilityMethods.kebabCase(identifier.substring(11));
                final @Nullable var profession = MMOCore.plugin.professionManager.get(professionId);
                Validate.notNull(profession, "Profession not found with ID " + professionId);
                return String.valueOf(playerData.getCollectionSkills().getLevel(profession));
            }

            // Current Experience
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("experience"))
                return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getExperience());

            // Experience needed for next level
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("next_level"))
                return String.valueOf(playerData.getLevelUpExperience());

            // Class point count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("class_points"))
                return String.valueOf(playerData.getClassPoints());

            // Skill point count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("skill_points"))
                return String.valueOf(playerData.getSkillPoints());

            // Attribute point count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("attribute_points"))
                return String.valueOf(playerData.getAttributePoints());

            // Attribute reallocation point count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("attribute_reallocation_points"))
                return String.valueOf(playerData.getAttributeReallocationPoints());

            // Total attribute value, including all points spent and modifiers
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("attribute_"))
                return String.valueOf(playerData.getAttributes()
                        .getAttribute(MMOCore.plugin.attributeManager.get(identifier.substring(10).toLowerCase().replace("_", "-"))));

            // Player mana
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("mana"))
                return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getMana());

            // Mana bar
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("mana_bar"))
                return playerData.getProfess().getManaDisplay().generateBar(playerData.getMana(), playerData.getStats().getStat("MAX_MANA"));


            // Exp multiplier in a specific profession/main class
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("exp_multiplier_")) {
                String format = identifier.substring(15).toLowerCase().replace("_", "-").replace(" ", "-");
                Profession profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
                return MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.boosterManager.getMultiplier(profession) * 100);
            }

            // Exp boost (multiplier - 1) in a specific profession/main class
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("exp_boost_")) {
                String format = identifier.substring(10).toLowerCase().replace("_", "-").replace(" ", "-");
                Profession profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
                return MythicLib.plugin.getMMOConfig().decimal.format((MMOCore.plugin.boosterManager.getMultiplier(profession) - 1) * 100);
            }

            // Current stamina
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("stamina"))
                return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getStamina());

            // Formatted stamina bar
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("stamina_bar")) {
                StringBuilder format = new StringBuilder();
                double ratio = 20 * playerData.getStamina() / playerData.getStats().getStat("MAX_STAMINA");
                for (double j = 1; j < 20; j++)
                    format.append(ratio >= j ? MMOCore.plugin.configManager.staminaFull
                                    : ratio >= j - .5 ? MMOCore.plugin.configManager.staminaHalf : MMOCore.plugin.configManager.staminaEmpty)
                            .append(AltChar.listSquare);
                return format.toString();
            }

            // Stat value
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.startsWith("stat_")) {
                final String stat = UtilityMethods.enumName(identifier.substring(5));
                return StatManager.format(stat, playerData.getMMOPlayerData());
            }

            // Current stellium
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("stellium"))
                return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getStellium());

            // Formatted stellium bar
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("stellium_bar")) {
                StringBuilder format = new StringBuilder();
                double ratio = 20 * playerData.getStellium() / playerData.getStats().getStat("MAX_STELLIUM");
                for (double j = 1; j < 20; j++)
                    format.append(ratio >= j ? ChatColor.BLUE : ratio >= j - .5 ? ChatColor.AQUA : ChatColor.WHITE).append(AltChar.listSquare);
                return format.toString();
            }

            // Current quest
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("quest")) {
                PlayerQuests data = playerData.getQuestData();
                return data.hasCurrent() ? data.getCurrent().getQuest().getName() : "";
            }

            // Quest progress
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("quest_progress")) {
                PlayerQuests data = playerData.getQuestData();
                return data.hasCurrent() ? MythicLib.plugin.getMMOConfig().decimal
                        .format((double) data.getCurrent().getObjectiveNumber() / data.getCurrent().getQuest().getObjectives().size() * 100L) : "0";
            }

            // Quest objective lore
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("quest_objective")) {
                PlayerQuests data = playerData.getQuestData();
                return data.hasCurrent() ? data.getCurrent().getFormattedLore() : "";
            }

            // Guild name
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("guild_name")) {
                if (playerData.getGuild() == null) return "";
                return playerData.getGuild().getName();
            }

            // Guild tag
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("guild_tag")) {
                if (playerData.getGuild() == null) return "";
                return playerData.getGuild().getTag();
            }

            // Guild leader
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equalsIgnoreCase("leader")) {
                if (playerData.getGuild() == null) return "";
                return Bukkit.getOfflinePlayer(playerData.getGuild().getOwner()).getName();
            }

            // Guild member count
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equalsIgnoreCase("guild_members")) {
                if (playerData.getGuild() == null) return "0";
                return String.valueOf(playerData.getGuild().countMembers());
            }

            // Online guild members
            /////////////////////////////////////////////////////////////////////////////////////////////////
            if (identifier.equals("guild_online_members")) {
                if (playerData.getGuild() == null) return "0";
                return String.valueOf(playerData.getGuild().countOnlineMembers());
            }

        } catch (Exception exception) {
            MMOCore.log(Level.WARNING, "Error while parsing placeholder '" + identifier + "':");
            exception.printStackTrace();
            return ERROR_PLACEHOLDER;
        }

        MMOCore.log(Level.WARNING, "Could not match placeholder '" + identifier + "'");
        return NO_MATCH_PLACEHOLDER;
    }
}
