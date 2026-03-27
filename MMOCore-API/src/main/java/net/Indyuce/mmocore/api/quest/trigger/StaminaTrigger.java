package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger.Operation;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;

public class StaminaTrigger extends Trigger {
    private final RandomAmount amount;
    private final Operation operation;

    public StaminaTrigger(MMOLineConfig config) {
        super(config);

        config.validate("amount");
        amount = new RandomAmount(config.getString("amount"));
        operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
    }

    @Override
    public void apply(PlayerData player) {

        // Give stamina
        if (operation == Operation.GIVE)
            player.giveStamina(amount.calculate(), ResourceUpdateReason.MECHANIC);

            // Set stamina
        else if (operation == Operation.SET)
            player.setStamina(amount.calculate(), ResourceUpdateReason.MECHANIC);

            // Take stamina
        else player.giveStamina(-amount.calculate(), ResourceUpdateReason.MECHANIC);
    }
}
