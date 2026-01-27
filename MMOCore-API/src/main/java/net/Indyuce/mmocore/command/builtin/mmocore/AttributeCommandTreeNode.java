package net.Indyuce.mmocore.command.builtin.mmocore;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AttributeCommandTreeNode extends CommandTreeNode {
    public AttributeCommandTreeNode(CommandTreeNode parent) {
        super(parent, "attribute");

        addChild(new ActionCommandTreeNode(this, "give", 1));
        addChild(new ActionCommandTreeNode(this, "take", -1));
        addChild(new CheckCommandTreeNode(this));
    }

    static class CheckCommandTreeNode extends CommandTreeNode {
        private final Argument<Player> argPlayer;
        private final Argument<PlayerAttribute> argAttribute;

        public CheckCommandTreeNode(CommandTreeNode parent) {
            super(parent, "check");

            argPlayer = addArgument(Argument.PLAYER);
            argAttribute = addArgument(Arguments.ATTRIBUTE);
        }

        @Override
        public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var player = explorer.parse(argPlayer);
            final var attribute = explorer.parse(argAttribute);

            PlayerAttributes.AttributeInstance instance = PlayerData.get(player).getAttributes().getInstance(attribute);
            return explorer.success("&6" + player.getName() + "&e has &6" + instance.getBase() + "&e points spent in " + attribute.getName() + " (total = &6" + instance.getTotal() + "&e).");
        }
    }

    static class ActionCommandTreeNode extends CommandTreeNode {
        private final Argument<Player> argPlayer;
        private final Argument<PlayerAttribute> argAttribute;
        private final Argument<Integer> argAmount;

        private final int c;

        public ActionCommandTreeNode(CommandTreeNode parent, String type, int coef) {
            super(parent, type);

            this.c = coef;

            argPlayer = addArgument(Argument.PLAYER);
            argAttribute = addArgument(Arguments.ATTRIBUTE);
            argAmount = addArgument(Argument.AMOUNT_INT);
        }

        @Override
        public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var player = explorer.parse(argPlayer);
            final var attribute = explorer.parse(argAttribute);
            final var amount = explorer.parse(argAmount);

            PlayerAttributes.AttributeInstance instance = PlayerData.get(player).getAttributes().getInstance(attribute);
            instance.setBase(Math.min(attribute.getMax(), instance.getBase() + c * amount));
            return explorer.success("&6" + player.getName() + "&e now has &6" + instance.getBase() + "&e points in " + attribute.getName() + ".");
        }
    }
}
