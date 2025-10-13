package net.Indyuce.mmocore.command.builtin.mmocore.skill;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LockCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<RegisteredSkill> argSkill;

    public LockCommandTreeNode(CommandTreeNode parent, String id) {
        super(parent, id);

        argPlayer = addArgument(Argument.PLAYER);
        argSkill = addArgument(Arguments.SKILL);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);
        RegisteredSkill regSkill = explorer.parse(argSkill);

        ClassSkill skill = playerData.getProfess().getSkill(args[4]);
        if (skill == null) {
            return explorer.fail("Class " + playerData.getProfess().getName() + " doesn't have a skill called '" + regSkill.getName() + "'");
        }

        if (!playerData.hasUnlocked(skill)) {
            return explorer.fail("Skill " + skill.getSkill().getName() + " already locked for " + player.getName());
        }

        playerData.lock(skill);
        return explorer.success("Skill &6" + skill.getSkill().getName() + "&e unlocked for &6" + player.getName());
    }
}
