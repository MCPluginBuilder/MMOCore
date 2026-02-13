package net.Indyuce.mmocore.command.builtin.mmocore.cast;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirstCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argIndex;

    public FirstCommandTreeNode(CommandTreeNode parent) {
        super(parent, "first");

        argPlayer = addArgument(Argument.PLAYER);
        argIndex = addArgument(Arguments.INDEX);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData data = PlayerData.get(player);

        int slot = explorer.parse(argIndex);

        List<Integer> slots = data.getBoundSkills().entrySet().stream()
                .filter(e -> !e.getValue().isPassive())
                .map(Map.Entry::getKey)
                .sorted(Integer::compare).collect(Collectors.toList());
        if (slot > slots.size()) return explorer.fail("Player " + player.getName() + " only has active skills on slots " + slots + ".");

        var skill = data.getBoundSkill(slots.get(slot - 1));
        Validate.notNull(skill, "Internal error: skill is null");
        Validate.isTrue(!skill.getTrigger().isPassive(), "Internal error: skill is passive");

        boolean success = skill.toCastable(data).cast(data.getMMOPlayerData()).isSuccessful();
        return success ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }
}
