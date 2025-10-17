package net.Indyuce.mmocore.manager.data.sql;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.DataLoadResult;
import io.lumine.mythic.lib.data.SaveReason;
import io.lumine.mythic.lib.data.sql.SQLDatabase;
import io.lumine.mythic.lib.gson.JsonArray;
import io.lumine.mythic.lib.gson.JsonElement;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLDatabaseImpl extends SQLDatabase<PlayerData, OfflinePlayerData> {
    public static final String UUID_FIELD_NAME = "uuid";

    public SQLDatabaseImpl() {
        super(MMOCore.plugin,  UUID_FIELD_NAME);
    }

    private static final String[] NEW_COLUMNS = new String[]{
            "times_claimed", "LONGTEXT",
            "is_saved", "TINYINT",
            "skill_reallocation_points", "INT(11)",
            "skill_tree_reallocation_points", "INT(11)",
            "skill_tree_points", "LONGTEXT",
            "skill_tree_levels", "LONGTEXT",
            "unlocked_items", "LONGTEXT",
            "health", "FLOAT",
            "mana", "FLOAT",
            "stamina", "FLOAT",
            "stellium", "FLOAT"};

    @Override
    public void setup() {

        // Fully create table
        executeUpdate("CREATE TABLE IF NOT EXISTS " + userdataTableName + "("
                + UUID_FIELD_NAME + " VARCHAR(36)," +
                "class_points INT(11) DEFAULT 0," +
                "skill_points INT(11) DEFAULT 0," +
                "attribute_points INT(11) DEFAULT 0," +
                "attribute_realloc_points INT(11) DEFAULT 0," +
                "skill_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_reallocation_points INT(11) DEFAULT 0," +
                "skill_tree_points LONGTEXT," +
                "skill_tree_levels LONGTEXT," +
                "level INT(11) DEFAULT 1," +
                "experience DOUBLE PRECISION DEFAULT 0," +
                "class VARCHAR(20)," +
                "guild VARCHAR(20)," +
                "last_login LONG," +
                "attributes LONGTEXT," +
                "professions LONGTEXT," +
                "times_claimed LONGTEXT," +
                "quests LONGTEXT," +
                "waypoints LONGTEXT," +
                "friends LONGTEXT," +
                "skills LONGTEXT," +
                "bound_skills LONGTEXT," +
                "health FLOAT," +
                "mana FLOAT," +
                "stamina FLOAT," +
                "stellium FLOAT," +
                "unlocked_items LONGTEXT," +
                "class_info LONGTEXT," +
                "is_saved TINYINT," +
                "PRIMARY KEY (uuid));");

        // Add columns that might not be here by default
        for (int i = 0; i < NEW_COLUMNS.length; i += 2) {
            final var columnName = NEW_COLUMNS[i];
            final var dataType = NEW_COLUMNS[i + 1];
            executeQuery("SELECT * FROM `information_schema`.`COLUMNS` WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?", result -> {
                if (!result.next())
                    executeUpdate("ALTER TABLE " + userdataTableName + " ADD COLUMN " + columnName + " " + dataType);
            }, databaseName, userdataTableName, columnName);
        }

        // Modify exp to be a double precision instead
        executeUpdate("ALTER TABLE `" + userdataTableName + "` MODIFY COLUMN experience DOUBLE PRECISION");
    }

    @Override
    protected @NotNull DataLoadResult loadDataFromResultSet(@NotNull PlayerData playerData, @NotNull ResultSet result, boolean force) throws SQLException {

        // Reset stats linked to triggers
        playerData.resetTriggerStats();

        playerData.setClassPoints(result.getInt("class_points"));
        playerData.setSkillPoints(result.getInt("skill_points"));
        playerData.setSkillReallocationPoints(result.getInt("skill_reallocation_points"));
        playerData.setSkillTreeReallocationPoints(result.getInt("skill_tree_reallocation_points"));
        playerData.setAttributePoints(result.getInt("attribute_points"));
        playerData.setAttributeReallocationPoints(result.getInt("attribute_realloc_points"));
        playerData.setLevel(result.getInt("level"), PlayerLevelChangeEvent.Reason.CHOOSE_PROFILE);
        playerData.setExperience(result.getDouble("experience"));

        if (!isEmpty(result.getString("class")))
            playerData.setClass(MMOCore.plugin.classManager.get(result.getString("class")));

        if (!isEmpty(result.getString("times_claimed"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("times_claimed"), JsonObject.class);
            json.entrySet().forEach(entry -> playerData.getItemClaims().put(entry.getKey(), entry.getValue().getAsInt()));
        }
        if (!isEmpty(result.getString("skill_tree_points"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("skill_tree_points"), JsonObject.class);
            for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll()) {
                playerData.setSkillTreePoints(skillTree.getId(), json.has(skillTree.getId()) ? json.get(skillTree.getId()).getAsInt() : 0);
            }
            playerData.setSkillTreePoints("global", json.has("global") ? json.get("global").getAsInt() : 0);
        }

        if (!isEmpty(result.getString("skill_tree_levels"))) {
            JsonObject json = MythicLib.plugin.getGson().fromJson(result.getString("skill_tree_levels"), JsonObject.class);
            for (SkillTreeNode skillTreeNode : MMOCore.plugin.skillTreeManager.getAllNodes()) {
                playerData.setNodeLevel(skillTreeNode, json.has(skillTreeNode.getFullId()) ? json.get(skillTreeNode.getFullId()).getAsInt() : 0);
            }
        }
        Set<String> unlockedItems = new HashSet<>();
        if (!isEmpty(result.getString("unlocked_items"))) {
            JsonArray unlockedItemsArray = MythicLib.plugin.getGson().fromJson(result.getString("unlocked_items"), JsonArray.class);
            for (JsonElement item : unlockedItemsArray)
                unlockedItems.add(item.getAsString());
        }
        playerData.setUnlockedItems(unlockedItems);
        if (!isEmpty(result.getString("guild"))) {
            final Guild guild = MMOCore.plugin.nativeGuildManager.getGuild(result.getString("guild"));
            if (guild != null && guild.hasMember(playerData.getUniqueId())) playerData.setGuild(guild);
        }
        if (!isEmpty(result.getString("attributes"))) playerData.getAttributes().load(result.getString("attributes"));
        if (playerData.isOnline())
            MMOCore.plugin.attributeManager.getAll().forEach(attribute -> playerData.getAttributes().getInstance(attribute).updateStats());
        if (!isEmpty(result.getString("professions")))
            playerData.getCollectionSkills().load(result.getString("professions"));
        if (!isEmpty(result.getString("quests"))) playerData.getQuestData().load(result.getString("quests"));
        playerData.getQuestData().updateBossBar();
        if (!isEmpty(result.getString("waypoints")))
            playerData.getWaypoints().addAll(MMOCoreUtils.jsonArrayToList(result.getString("waypoints")));
        if (!isEmpty(result.getString("friends")))
            MMOCoreUtils.jsonArrayToList(result.getString("friends")).forEach(str -> playerData.getFriends().add(UUID.fromString(str)));
        if (!isEmpty(result.getString("skills"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("skills"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
                playerData.setSkillLevel(entry.getKey(), entry.getValue().getAsInt());
        }
        if (!isEmpty(result.getString("bound_skills"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("bound_skills"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                ClassSkill skill = playerData.getProfess().getSkill(entry.getValue().getAsString());
                if (skill != null) playerData.bindSkill(Integer.parseInt(entry.getKey()), skill);
            }
        }
        if (!isEmpty(result.getString("class_info"))) {
            JsonObject object = MythicLib.plugin.getGson().fromJson(result.getString("class_info"), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                try {
                    PlayerClass profess = MMOCore.plugin.classManager.get(entry.getKey());
                    Validate.notNull(profess, "Could not find class '" + entry.getKey() + "'");
                    playerData.applyClassInfo(profess, new SavedClassInformation(entry.getValue().getAsJsonObject()));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load class info '" + entry.getKey() + "': " + exception.getMessage());
                }
            }
        }

        /*
         * These should be loaded after to make sure that the
         * MAX_MANA, MAX_STAMINA & MAX_STELLIUM stats are already loaded.
         */
        playerData.loadResources(result.getDouble("health"), result.getDouble("mana"), result.getDouble("stamina"), result.getDouble("stellium"));

        UtilityMethods.debug(MMOCore.plugin, "SQL", String.format("{ class: %s, level: %d }", playerData.getProfess().getId(), playerData.getLevel()));
        return new DataLoadResult(DataLoadResult.Type.SUCCESS, false, force);
    }

    @Override
    public void saveData(PlayerData data, @NotNull SaveReason saveReason) {
        final UUID effectiveId = data.getEffectiveId();
        UtilityMethods.debug(MMOCore.plugin, "SQL", "Saving data for: '" + effectiveId + "'...");

        final PlayerDataTableUpdater updater = new PlayerDataTableUpdater(this, data);
        updater.addData("class_points", data.getClassPoints());
        updater.addData("skill_points", data.getSkillPoints());
        updater.addData("skill_reallocation_points", data.getSkillReallocationPoints());
        updater.addData("attribute_points", data.getAttributePoints());
        updater.addData("attribute_realloc_points", data.getAttributeReallocationPoints());
        updater.addData("skill_tree_reallocation_points", data.getSkillTreeReallocationPoints());
        updater.addData("health", data.getLastHealth());
        updater.addData("mana", data.getMana());
        updater.addData("stellium", data.getStellium());
        updater.addData("stamina", data.getStamina());
        updater.addData("level", data.getLevel());
        updater.addData("experience", data.getExperience());
        updater.addData("class", data.getProfess().getId());
        updater.addData("last_login", data.getLastLogin());
        updater.addData("guild", data.hasGuild() ? data.getGuild().getId() : null);
        updater.addJSONArray("waypoints", data.getWaypoints());
        updater.addJSONArray("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
        updater.addJSONObject("bound_skills", data.mapBoundSkills().entrySet());
        updater.addJSONObject("skills", data.mapSkillLevels().entrySet());
        updater.addJSONObject("times_claimed", data.getItemClaims().entrySet());
        updater.addJSONObject("skill_tree_points", data.mapSkillTreePoints().entrySet());
        updater.addJSONObject("skill_tree_levels", data.getNodeLevelsEntrySet());
        updater.addData("attributes", data.getAttributes().toJson().toString());
        updater.addData("professions", data.getCollectionSkills().toJsonString());
        updater.addData("quests", data.getQuestData().toJsonString());
        updater.addData("class_info", createClassInfoData(data).toString());
        updater.addJSONArray("unlocked_items", data.getUnlockedItems());
        if (saveReason != SaveReason.AUTOSAVE) updater.addData("is_saved", 1);

        updater.executeRequest(saveReason);

        UtilityMethods.debug(MMOCore.plugin, "SQL", "Saved data for: " + effectiveId);
        UtilityMethods.debug(MMOCore.plugin, "SQL", String.format("{ class: %s, level: %d }", data.getProfess().getId(), data.getLevel()));
    }

    private JsonObject createClassInfoData(PlayerData playerData) {
        final JsonObject json = new JsonObject();
        for (String c : playerData.getSavedClasses()) {
            final SavedClassInformation info = playerData.getClassInfo(c);
            JsonObject classinfo = new JsonObject();
            classinfo.addProperty("level", info.getLevel());
            classinfo.addProperty("experience", info.getExperience());
            classinfo.addProperty("skill-points", info.getSkillPoints());
            classinfo.addProperty("attribute-points", info.getAttributePoints());
            classinfo.addProperty("attribute-realloc-points", info.getAttributeReallocationPoints());
            classinfo.addProperty("skill-reallocation-points", info.getSkillReallocationPoints());
            classinfo.addProperty("skill-tree-reallocation-points", info.getSkillTreeReallocationPoints());
            classinfo.addProperty("health", info.getLastHealth());
            classinfo.addProperty("mana", info.getMana());
            classinfo.addProperty("stamina", info.getStamina());
            classinfo.addProperty("stellium", info.getStellium());

            JsonArray array = new JsonArray();
            for (String unlockedItem : playerData.getUnlockedItems()) {
                array.add(unlockedItem);
            }
            classinfo.add("unlocked-items", array);

            JsonObject skillinfo = new JsonObject();
            for (String skill : info.getSkillKeys())
                skillinfo.addProperty(skill, info.getSkillLevel(skill));
            classinfo.add("skill", skillinfo);

            JsonObject attributeInfo = new JsonObject();
            for (String attribute : info.getAttributeKeys())
                attributeInfo.addProperty(attribute, info.getAttributeLevel(attribute));
            classinfo.add("attribute", attributeInfo);

            JsonObject nodeLevelsInfo = new JsonObject();
            for (String node : info.getNodeKeys())
                nodeLevelsInfo.addProperty(node, info.getNodeLevel(node));
            classinfo.add("node-levels", nodeLevelsInfo);

            JsonObject skillTreePointsInfo = new JsonObject();
            for (String skillTreeId : info.getSkillTreePointsKeys())
                skillTreePointsInfo.addProperty(skillTreeId, info.getSkillTreePoints(skillTreeId));
            classinfo.add("skill-tree-points", skillTreePointsInfo);

            JsonObject boundSkillInfo = new JsonObject();
            for (int slot : info.mapBoundSkills().keySet())
                boundSkillInfo.addProperty(String.valueOf(slot), info.mapBoundSkills().get(slot));
            classinfo.add("bound-skills", boundSkillInfo);

            json.add(c, classinfo);
        }

        return json;
    }

    private boolean isEmpty(@Nullable String str) {
        return str == null
                || str.isEmpty()
                || str.equalsIgnoreCase("null")
                || str.equalsIgnoreCase("{}")
                || str.equalsIgnoreCase("[]");
    }

    @NotNull
    @Override
    public OfflinePlayerData getOffline(UUID uuid) {
        return new SQLOfflinePlayerData(uuid);
    }
}



