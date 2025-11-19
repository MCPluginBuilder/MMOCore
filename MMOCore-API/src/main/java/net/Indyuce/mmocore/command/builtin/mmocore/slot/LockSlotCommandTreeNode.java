package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LockSlotCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argIndex;

    public LockSlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "lock");

        argPlayer = addArgument(Argument.PLAYER);
        argIndex = addArgument(Arguments.INDEX);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final Player player = explorer.parse(argPlayer);
        final PlayerData playerData = PlayerData.get(player);
        final int slot = explorer.parse(argIndex);

        if (slot <= 0) {
            return explorer.fail("The slot can't be negative.");
        }
        SkillSlot skillSlot = playerData.getProfess().getSkillSlot(slot);
        if (skillSlot == null) {
            return explorer.fail("Skill slot with index " + slot + " was not found for player " + player.getName() + " with class " + playerData.getProfess().getId());
        }

        if (skillSlot.isUnlockedByDefault()) {
            sender.sendMessage(ChatColor.RED + "This skill slot is unlocked by default.");
            return CommandResult.FAILURE;
        }

        if (!playerData.hasUnlocked(skillSlot)) {
            return explorer.fail("Skill slot " + skillSlot.getName() + " is already locked for " + player.getName());
        }

        playerData.lock(skillSlot);
        return explorer.success("Skill slot &6" + skillSlot.getName() + "&e is now locked for &6" + player.getName());
    }
}
