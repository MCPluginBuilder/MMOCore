package net.Indyuce.mmocore.command.builtin.mmocore.skill;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnlockCommandTreeNode extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<SkillHandler<?>> argSkill;

    public UnlockCommandTreeNode(CommandTreeNode parent, String id) {
        super(parent, id);

        argPlayer = addArgument(Argument.PLAYER);
        argSkill = addArgument(Argument.SKILL_HANDLER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);
        var regSkill = explorer.parse(argSkill);

        ClassSkill skill = playerData.getProfess().getSkill(regSkill);
        if (skill == null) {
            return explorer.fail("Class does not have skill '" + regSkill.getName() + "'");
        }

        if (playerData.hasUnlocked(skill)) {
            return explorer.fail("Skill " + skill.getSkill().getName() + " already unlocked for " + player.getName());
        }

        playerData.unlock(skill);
        return explorer.success("Skill &6" + skill.getSkill().getName() + "&e unlocked for &6" + player.getName());
    }
}
