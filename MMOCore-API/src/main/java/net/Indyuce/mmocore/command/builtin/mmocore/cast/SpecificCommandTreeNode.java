package net.Indyuce.mmocore.command.builtin.mmocore.cast;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpecificCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argIndex;

    public SpecificCommandTreeNode(CommandTreeNode parent) {
        super(parent, "specific");

        argPlayer = addArgument(Argument.PLAYER);
        argIndex = addArgument(Arguments.INDEX);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData data = PlayerData.get(player);

        int slot = explorer.parse(argIndex);

        ClassSkill skill = data.getBoundSkill(slot);
        if (skill == null)
            return explorer.fail("Found no skill bound to slot " + slot + " of player " + player.getName() + ".");

        if (skill.getTrigger().isPassive())
            return explorer.fail("Skill '" + skill.getSkill().getName() + "' bound to slot " + slot + " is passive.");

        boolean success = skill.toCastable(data).cast(data.getMMOPlayerData()).isSuccessful();
        return success ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
