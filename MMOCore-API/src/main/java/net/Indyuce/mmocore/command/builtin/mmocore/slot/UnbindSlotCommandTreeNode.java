package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnbindSlotCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argSlot;

    public UnbindSlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "unbind");

        argPlayer = addArgument(Argument.PLAYER);
        argSlot = addArgument(Argument.AMOUNT_INT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);
        int slot = explorer.parse(argSlot);

        final BoundSkillInfo found = playerData.unbindSkill(slot);
        return explorer.success((found != null ?
                "Skill &6" + found.getClassSkill().getSkill().getName() + "&e was taken off the slot &6" + slot :
                "Could not find skill at slot &6" + slot));
    }
}