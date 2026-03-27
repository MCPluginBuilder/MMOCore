package net.Indyuce.mmocore.script.mechanic;

import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class StaminaMechanic extends TargetMechanic {
    private final NumericExpression amount;
    private final Operation operation;
    private final ResourceUpdateReason reason;

    public StaminaMechanic(ConfigObject config) {
        super(config);

        amount = config.numericExpr("amount");
        reason = config.parse(ResourceUpdateReason.MECHANIC, Parsers.RESOURCE_UPDATE_REASON, "reason");
        operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        PlayerData targetData = PlayerData.get(target.getUniqueId());
        if (operation == Operation.GIVE) targetData.giveStamina(amount.evaluate(meta), reason);
        else if (operation == Operation.SET) targetData.setStamina(amount.evaluate(meta), reason);
        else if (operation == Operation.TAKE) targetData.giveStamina(-amount.evaluate(meta), reason);
    }
}
