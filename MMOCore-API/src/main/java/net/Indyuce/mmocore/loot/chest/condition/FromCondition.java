package net.Indyuce.mmocore.loot.chest.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;

import java.util.ArrayList;
import java.util.List;

public class FromCondition extends Condition {
    private final List<Condition> conditions = new ArrayList<>();


    public FromCondition(MMOLineConfig config) {
        super(config);

        var list = new YamlFile(MMOCore.plugin, "conditions").getContent().getStringList(config.getString("source"));
        Validate.isTrue(!list.isEmpty(), "There is no source matching " + config.getString("key"));
        list.stream()
                .map(MMOLineConfig::new)
                .forEach(mmoLineConfig ->
                        conditions.add(MMOCore.plugin.loadManager.loadCondition(mmoLineConfig)));
    }


    @Override
    public boolean isMet(ConditionInstance entity) {
        return conditions.stream().allMatch(condition -> condition.isMet(entity));
    }
}
