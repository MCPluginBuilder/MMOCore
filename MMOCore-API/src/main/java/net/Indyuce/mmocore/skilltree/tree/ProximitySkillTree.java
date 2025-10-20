package net.Indyuce.mmocore.skilltree.tree;

import net.Indyuce.mmocore.skilltree.IntCoords;
import net.Indyuce.mmocore.skilltree.ParentInformation;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.configuration.ConfigurationSection;

public class ProximitySkillTree extends SkillTree {
    public ProximitySkillTree(ConfigurationSection config) {
        super(config);

        // Neighbors are marked as soft parents
        for (var node : nodes.values())
            for (var relative : RELATIVES) {
                final SkillTreeNode neighbor = this.getNodeOrNull(node.getCoordinates().add(relative));
                if (neighbor != null) {
                    final var parentInfo = new ParentInformation(node, neighbor, ParentType.SOFT, true,1);
                    node.addParent(parentInfo);
                }
            }
    }

    private static final IntCoords[] RELATIVES = {
            new IntCoords(1, 0),
            new IntCoords(-1, 0),
            new IntCoords(0, 1),
            new IntCoords(0, -1)
    };
}
