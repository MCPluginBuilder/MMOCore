package net.Indyuce.mmocore.manager.data.sql;

import io.lumine.mythic.lib.gson.JsonArray;
import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDatabaseImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlayerDataTableUpdater {
    private final PlayerData playerData;
    private final SQLDatabaseImpl provider;
    private final UUID effectiveId;
    private final Map<String, String> requestMap = new HashMap<>();

    public PlayerDataTableUpdater(SQLDatabaseImpl provider, PlayerData playerData) {
        this.playerData = playerData;
        this.provider = provider;
        this.effectiveId = playerData.getEffectiveId();
    }

    public void executeRequest(@NotNull SessionUpdateReason saveReason) {
        final String request = "INSERT INTO " + provider.getUserDataTableName() + "(" + SQLDatabaseImpl.UUID_FIELD_NAME + ", " + formatCollection(requestMap.keySet(), false)
                + ") VALUES('" + effectiveId + "'," + formatCollection(requestMap.values(), true) + ")" +
                " ON DUPLICATE KEY UPDATE " + formatMap() + ";";

        try (var connection = provider.getConnection();
             var statement = connection.prepareStatement(request)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            MMOCore.log(Level.WARNING, "Could not save player data of " + effectiveId + ", saving to YAML instead");
            exception.printStackTrace();

            new YAMLDatabaseImpl().saveData(playerData, saveReason);
        }
    }

    public void addData(@NotNull String key, @Nullable Object value) {
        requestMap.put(key, String.valueOf(value));
    }

    public String formatCollection(Collection<String> strings, boolean withComma) {
        StringBuilder values = new StringBuilder();
        for (String key : strings) {
            if (withComma)
                values.append("'");
            values.append(key);
            if (withComma)
                values.append("'");
            values.append(",");
        }
        //Remove the last coma
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }

    public String formatMap() {
        final StringBuilder values = new StringBuilder();
        for (String key : requestMap.keySet())
            values.append(key).append("='").append(requestMap.get(key)).append("',");

        // Remove the last comma
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }

    public void addJSONArray(String key, Collection<String> collection) {
        JsonArray json = new JsonArray();
        for (String s : collection)
            json.add(s);
        addData(key, json.toString());
    }

    public <T, V> void addJSONObject(String key, Set<Map.Entry<T, V>> collection) {
        JsonObject json = new JsonObject();
        for (Map.Entry<T, V> entry : collection)
            json.addProperty(entry.getKey().toString(), entry.getValue().toString());
        addData(key, json.toString());
    }
}
