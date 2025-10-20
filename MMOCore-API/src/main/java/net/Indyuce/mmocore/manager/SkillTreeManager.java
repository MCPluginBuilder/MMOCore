package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.manager.registry.MMOCoreRegister;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.skilltree.tree.SkillTreeType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public class SkillTreeManager extends MMOCoreRegister<SkillTree> {
    private final Map<String, SkillTreeNode> skillTreeNodes = new HashMap<>();

    @Override
    public void register(@NotNull SkillTree tree) {
        super.register(tree);
        tree.getNodes().forEach((node) -> skillTreeNodes.put(node.getFullId(), node));
    }

    @Nullable
    public SkillTreeNode getNode(@NotNull String fullId) {
        return skillTreeNodes.get(fullId);
    }

    @NotNull
    public Collection<SkillTreeNode> getAllNodes() {
        return skillTreeNodes.values();
    }

    @Override
    public String getRegisteredObjectName() {
        return "skill tree";
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            registered.clear();
            skillTreeNodes.clear();
        }

        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "skill-trees", true,
                (key, config) -> register(loadSkillTree(config)), "Could not load skill tree from file '%s': %s");
    }

    @NotNull
    public SkillTree loadSkillTree(@NotNull ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");
        final var type = UtilityMethods.prettyValueOf(SkillTreeType::valueOf, config.getString("type", "custom"), "No skill tree type '%s'");
        return type.construct(config);
    }

    @Deprecated
    public SkillTree get(int index) {
        return new ArrayList<>(registered.values()).get(index);
    }

    /**
     * Useful to recursively go through skill trees
     *
     * @return The list of all the roots (e.g the nodes without any parents
     */
    @Deprecated
    public List<SkillTreeNode> getRootNodes() {
        return skillTreeNodes.values().stream().filter(treeNode -> treeNode.getParents(ParentType.SOFT).isEmpty()).collect(Collectors.toList());
    }

    @Deprecated
    public boolean has(int index) {
        return index >= 0 && index < registered.size();
    }
}
