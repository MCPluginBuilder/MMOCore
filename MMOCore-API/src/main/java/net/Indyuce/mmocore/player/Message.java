package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.message.PlayerMessage;
import io.lumine.mythic.lib.message.ReadyMessage;
import io.lumine.mythic.lib.message.type.EmptyMessage;
import io.lumine.mythic.lib.util.ConfigFile;
import io.lumine.mythic.lib.util.config.YamlUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.util.Language;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public enum Message {

    // Combat
    NOW_IN_COMBAT,
    LEAVE_COMBAT,

    // Guild
    GUILD_CHAT,
    TRANSFER_GUILD_OWNERSHIP,
    GUILD_INVITE,
    GUILD_JOINED,
    GUILD_INVITE_COOLDOWN,
    GUILD_NOT_ONLINE_PLAYER("not-online-player"),
    GUILD_IS_FULL,
    GUILD_KICK_PLAYER("kick-from-guild"),
    GUILD_JOINED_OTHER,
    ALREADY_IN_GUILD,
    SENT_GUILD_INVITE,
    GUILD_FAIL_CREATION_INVALID_CHARS("fail-guild-creation.invalid-characters"),
    GUILD_FAIL_CREATION_INVALID_LENGTH("fail-guild-creation.invalid-length"),
    GUILD_FAIL_CREATION_ALREADY_EXISTS("fail-guild-creation.already-exists"),
    INPUT_GUILD_INVITE("player-input.chat.guild-invite"),
    INPUT_CANCEL_GUILD_INVITE("player-input.chat.guild-invite-cancel"),
    INPUT_GUILD_CREATION_TAG("player-input.chat.guild-creation-tag"),
    INPUT_CANCEL_GUILD_CREATION_TAG("player-input.chat.guild-creation-tag-cancel"),
    INPUT_GUILD_CREATION_NAME("player-input.chat.guild-creation-name"),
    INPUT_CANCEL_GUILD_CREATION_NAME("player-input.chat.guild-creation-name-cancel"),

    // Attributes
    ATTRIBUTE_MISSING_POINT("not-attribute-point"),
    ATTRIBUTE_MISSING_POINT_SHIFT("not-attribute-point-shift"),
    ATTRIBUTE_MAX_POINTS_HIT,
    ATTRIBUTE_LEVEL_UP,
    ATTRIBUTE_NO_POINTS_SPENT("no-attribute-points-spent"),
    ATTRIBUTE_POINTS_REALLOCATED,
    ATTRIBUTE_MISSING_REALLOCATION_POINT("not-attribute-reallocation-point"),

    // Quest
    QUEST_CANCEL("cancel-quest"),
    QUEST_ALREADY_ACTIVE("already-on-quest"),
    QUEST_LEVEL_RESTRICTION,
    QUEST_PROFESSION_LEVEL_RESTRICTION,
    QUEST_CANNOT_REDO("cant-redo-quest"),
    QUEST_COOLDOWN,
    QUEST_START("start-quest"),

    // Friends
    FRIEND_REQUEST,
    FRIEND_REQUEST_COOLDOWN,
    FRIEND_NOT_ONLINE_PLAYER("not-online-player"),
    FRIEND_CANT_FRIEND_YOURSELF("cant-request-to-yourself"),
    FRIEND_SENT_REQUEST("sent-friend-request"),
    FRIEND_REMOVED("no-longer-friends"),
    FRIEND_REQUEST_DENIED,
    FRIEND_REQUEST_DENIED_CREATOR,
    FRIEND_ALREADY("already-friends"),
    FRIEND_NOW("now-friends"),
    INPUT_FRIEND_REQUEST("player-input.chat.friend-request"),
    INPUT_CANCEL_FRIEND_REQUEST("player-input.chat.friend-request-cancel"),

    // Party
    PARTY_CHAT,
    PARTY_CREATED,
    PARTY_TRANSFER_OWNERSHIP("transfer-party-ownership"),
    PARTY_IS_FULL,
    PARTY_NOT_ONLINE_PLAYER("not-online-player"),
    PARTY_INVITE_COOLDOWN,
    PARTY_LEAVE,
    PARTY_SEND_INVITE("sent-party-invite"),
    PARTY_ALREADY_IN("already-in-party"),
    PARTY_JOINED_OTHER,
    PARTY_JOINED,
    PARTY_INVITE,
    PARTY_HIGH_LEVEL_DIFFERENCE("high-level-difference"),
    PARTY_KICKED_FROM("kicked-from-party"),
    PARTY_KICK_PLAYER("kick-from-party"),
    INPUT_PARTY_INVITE("player-input.chat.party-invite"),
    INPUT_CANCEL_PARTY_INVITE("player-input.chat.party-invite-cancel"),

    // Waypoints
    WAYPOINT_UNLOCK("waypoint.unlock", "new-waypoint"),
    WAYPOINT_UNLOCK_BOOK("waypoint.unlock-book", "new-waypoint-book"),
    WAYPOINT_MISSING_STELLIUM("waypoint.missing-stellium", "not-enough-stellium"),
    WAYPOINT_MISSING_ACCESS("waypoint.missing-access", "cannot-teleport-to"),
    WAYPOINT_ON_COOLDOWN("waypoint.on-cooldown", "waypoint-cooldown"),
    WAYPOINT_LOCKED("waypoint.locked", "not-unlocked-waypoint"),
    WAYPOINT_STANDING_ON("waypoint.standing-on", "standing-on-waypoint"),
    WAYPOINT_TP_CHARGE("waypoint.teleport.charge", "warping-comencing"),
    WAYPOINT_TP_DONE("waypoint.teleport.done"),
    WAYPOINT_TP_CANCEL("waypoint.teleport.cancel", "warping-canceled"),

    // Level, exp, class and professions
    LEVEL_UP,
    DEATH_EXP_LOSS,
    CLASS_SELECT,
    NEW_EXP_BOOSTER_MAIN("booster-main"),
    NEW_EXP_BOOSTER_PROFESSION("booster-skill"),
    CANT_CHOOSE_NEW_CLASS,
    NO_PERMISSION_FOR_CLASS,
    ALREADY_ON_CLASS,
    PROFESSION_LEVEL_UP,
    EXP_NOTIFICATION,

    // Skills
    NO_CLASS_SKILL,
    SKILL_LEVEL_NOT_MET,
    CASTING_NO_MANA("casting.no-mana"),
    CASTING_NO_STAMINA("casting.no-stamina"),
    CASTING_ON_COOLDOWN("casting.on-cooldown"),
    NO_SKILL_POINTS_SPENT,
    NOT_SKILL_REALLOCATION_POINT,
    SKILL_POINTS_REALLOCATED,
    NO_SKILL_BOUND,
    CANT_MANUALLY_BIND,
    SKILL_UNBOUND_FROM_SLOT,
    SKILL_BOUND_TO_SLOT,
    SKILL_CANNOT_BE_BOUND,
    SKILL_UI_FOCUS,
    NOT_COMPATIBLE_SKILL,
    CANNOT_UPGRADE_SKILL,
    SKILL_MAX_LEVEL_HIT,
    NOT_ENOUGH_SKILL_POINTS,
    NOT_ENOUGH_SKILL_POINTS_SHIFT,
    UPGRADE_SKILL,

    // Skill trees
    NO_SKILL_TREE,
    NO_SKILL_TREE_POINTS_SPENT,
    NOT_SKILL_TREE_REALLOCATION_POINT,
    SKILL_TREE_SWITCH,
    SKILL_TREE_REALLOCATE("reallocated-points"),
    SKILL_TREE_MAX_POINTS_SPENT("max-points-reached"),
    SKILL_TREE_UPGRADE_NODE("upgrade-skill-node"),
    MISSING_SKILL_NODE_PERMISSION,
    SKILL_TREE_NODE_LOCKED("locked-node"),
    SKILL_TREE_NODE_MAX_LEVEL_HIT("skill-node-max-level-hit"),
    NOT_ENOUGH_SKILL_TREE_POINTS,

    // Misc
    NOT_ENOUGH_PERMS,
    CLOSE_LOOT_CHEST,

    // Specific professions
    CANNOT_BREAK,

    // PvP Mode
    PVP_MODE_COOLDOWN("pvp-mode.cooldown"),
    PVP_MODE_TOGGLE_ON_SAFE("pvp-mode.toggle.on-safe"),
    PVP_MODE_TOGGLE_ON_INVULNERABLE("pvp-mode.toggle.on-invulnerable"),
    PVP_MODE_TOGGLE_OFF_SAFE("pvp-mode.toggle.off-safe"),
    PVP_MODE_ENTER_PVP_MODE_ON("pvp-mode.enter.pvp-mode-on"),
    PVP_MODE_ENTER_PVP_MODE_OFF("pvp-mode.enter.pvp-mode-off"),
    PVP_MODE_LEAVE_PVP_ALLOWED("pvp-mode.leave.pvp-allowed"),
    PVP_MODE_LEAVE_PVP_DENIED("pvp-mode.leave.pvp-denied"),
    PVP_MODE_CANNOT_HIT_HIGH_LEVEL_DIFFERENCE("pvp-mode.cannot-hit.high-level-difference"),
    PVP_MODE_CANNOT_HIT_LOW_LEVEL_TARGET("pvp-mode.cannot-hit.low-level-target"),
    PVP_MODE_CANNOT_HIT_LOW_LEVEL_SELF("pvp-mode.cannot-hit.low-level-self"),
    PVP_MODE_CANNOT_HIT_DISABLED_TARGET("pvp-mode.cannot-hit.pvp-mode-disabled-target"),
    PVP_MODE_CANNOT_HIT_DISABLED_SELF("pvp-mode.cannot-hit.pvp-mode-disabled-self"),
    PVP_MODE_CANNOT_HIT_INVULNERABLE_SELF("pvp-mode.cannot-hit.invulnerable-self"),
    PVP_MODE_CANNOT_HIT_INVULNERABLE_TARGET("pvp-mode.cannot-hit.invulnerable-target"),

    // Withdraw
    WITHDRAW_START("withdraw.prompt", "withdrawing"),
    WITHDRAW_CANCEL("withdraw.cancel"),
    WITHDRAW_SUCCESS("withdraw.success", "withdrew"),
    WITHDRAW_INVALID_AMOUNT("withdraw.invalid-amount", "wrong-number"),
    WITHDRAW_NOT_ENOUGH_MONEY("withdraw.not-enough-money", "not-enough-money"),

    // Deposit
    DEPOSIT_SUCCESS("deposit.success", "deposit"),


    ;

    private final String[] candidates;
    private PlayerMessage wrapped = new EmptyMessage();

    Message() {
        this.candidates = new String[]{UtilityMethods.kebabCase(name())};
    }

    Message(String... candidates) {
        var array = new String[candidates.length + 1];
        System.arraycopy(candidates, 0, array, 1, candidates.length);
        array[0] = UtilityMethods.kebabCase(name());
        this.candidates = array;
    }

    @NotNull
    public ReadyMessage prepare(@NotNull Object... placeholders) {
        return this.wrapped.prepare(null, placeholders);
    }

    public <T extends Player> void send(@NotNull Iterable<T> players, @NotNull Object... placeholders) {
        final var message = this.wrapped.prepare(null, placeholders);
        for (var player : players) message.send(player);
    }

    public void send(@NotNull Player player, @NotNull Object... placeholders) {
        this.send(PlayerData.get(player), placeholders);
    }

    public void send(@NotNull PlayerData playerData, @NotNull Object... placeholders) {
        this.wrapped.send(playerData.getMMOPlayerData(), null, placeholders);
    }

    public static void loadMessagesFromConfig() {
        var config = new ConfigFile(MMOCore.plugin, "messages").getConfig();

        // Load language entries
        Language.loadLanguageFromConfig(config);

        for (var message : values())
            try {
                final var objectFound = YamlUtils.get(config, message.candidates);
                Validate.notNull(objectFound, "Translation not found");
                message.wrapped = PlayerMessage.fromConfig(objectFound);
            } catch (Exception exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load message '" + message.candidates[0] + "': " + exception.getMessage());
                message.wrapped = new EmptyMessage(); // Safeguard
            }
    }
}
