package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BindSlotCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<Integer> argSlot;
    private final Argument<SkillHandler<?>> argSkill;

    public BindSlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "bind");

        argPlayer = addArgument(Argument.PLAYER);
        argSlot = addArgument(Arguments.INDEX);
        argSkill = addArgument(Argument.SKILL_HANDLER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);
        int slot = explorer.parse(argSlot);
        var skill = explorer.parse(argSkill);

        ClassSkill classSkill = playerData.getProfess().getSkill(skill);
        if (classSkill == null)
            return explorer.fail("The player's class doesn't have a skill called  " + args[5] + ".");
        playerData.bindSkill(slot, classSkill);

        return explorer.success("Skill &6" + classSkill.getSkill().getId() + "&e now bound to slot &6" + slot);
    }
}