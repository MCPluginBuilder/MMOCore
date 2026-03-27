package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger.Operation;
import net.Indyuce.mmocore.api.util.math.formula.RandomAmount;

public class StelliumTrigger extends Trigger {
    private final RandomAmount amount;
    private final Operation operation;

    public StelliumTrigger(MMOLineConfig config) {
        super(config);

        config.validate("amount");
        amount = new RandomAmount(config.getString("amount"));
        operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
    }

    @Override
    public void apply(PlayerData player) {

        // Give stellium
        if (operation == Operation.GIVE)
            player.giveStellium(amount.calculate(), ResourceUpdateReason.MECHANIC);

            // Set stellium
        else if (operation == Operation.SET)
            player.setStellium(amount.calculate(), ResourceUpdateReason.MECHANIC);

            // Take stellium
        else player.giveStellium(-amount.calculate(), ResourceUpdateReason.MECHANIC);
    }
}
