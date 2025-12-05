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

import java.util.function.BiFunction;

public class EditLevelCommandTreeNode extends CommandTreeNode {
    private final BiFunction<Integer, Integer, Integer> change;

    private final Argument<Player> argPlayer;
    private final Argument<SkillHandler<?>> argSkill;
    private final Argument<Integer> argLevel;

    public EditLevelCommandTreeNode(CommandTreeNode parent, String type, BiFunction<Integer, Integer, Integer> change) {
        super(parent, type);

        this.change = change;

        argPlayer = addArgument(Argument.PLAYER);
        argSkill = addArgument(Argument.SKILL_HANDLER);
        argLevel = addArgument(Arguments.INDEX.withKey("level"));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Player player = explorer.parse(argPlayer);
        PlayerData playerData = PlayerData.get(player);
        var skill = explorer.parse(argSkill);
        final var changeAmount = explorer.parse(argLevel);

        // Find equivalent class skill
        ClassSkill classSkill = null;
        for (ClassSkill var : playerData.getProfess().getSkills())
            if (var.getSkill().equals(skill)) {
                classSkill = var;
                break;
            }

        if (classSkill == null || classSkill.getUnlockLevel() > playerData.getLevel()) {
            return explorer.fail(skill.getName() + " is not unlockable for " + player.getName() + ".");
        }

        final var newValue = change.apply(playerData.getSkillLevel(skill), changeAmount);
        playerData.setSkillLevel(skill, newValue);
        return explorer.success("&6" + player.getName() + "&e is now level &6" + newValue + "&e for " + skill.getName());
    }
}
