package net.Indyuce.mmocore.command.builtin.mmocore.tree;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenCommandNode extends CommandTreeNode {
    private final Argument<SkillTree> argTree;
    private final Argument<Player> argPlayer;

    public OpenCommandNode(CommandTreeNode parent) {
        super(parent, "open");

        argPlayer = addArgument(Argument.PLAYER);
        argTree = addArgument(Arguments.SKILL_TREE_OR_GLOBAL);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var skillTree = explorer.parse(argTree);

        final var playerData = PlayerData.get(player);
        final var skillTrees = playerData.getProfess().getSkillTrees();
        if (skillTrees.isEmpty()) {
            return explorer.fail("Player class " + playerData.getProfess().getName() + " of " + player.getName() + " has no skill tree");
        }

        // Specific skill tree view
        if (skillTrees.stream().noneMatch(candidate -> skillTree.getId().equals(candidate.getId()))) {
            return explorer.fail("Player class " + playerData.getProfess().getName() + " of " + player.getName() + " has no skill tree " + skillTree.getId());
        }

        InventoryManager.SPECIFIC_TREE_VIEW.get(UtilityMethods.kebabCase(skillTree.getId())).newInventory(playerData).open();
        return explorer.success("Skill tree &6" + skillTree.getIcons() + "&e opened for player &6" + player.getName());
    }
}
