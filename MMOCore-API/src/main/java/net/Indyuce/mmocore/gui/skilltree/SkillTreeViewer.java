package net.Indyuce.mmocore.gui.skilltree;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skilltree.*;
import net.Indyuce.mmocore.skilltree.display.DisplayInfo;
import net.Indyuce.mmocore.skilltree.display.NodeDisplayInfo;
import net.Indyuce.mmocore.skilltree.display.PathDisplayInfo;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.util.Icon;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SkillTreeViewer extends EditableInventory {
    protected final Map<DisplayInfo, Icon> icons = new HashMap<>();
    protected final Map<NodeState, String> statusNames = new HashMap<>();

    /**
     * A null skillTree means the global skill tree view is opened.
     * Else this GUI represents a specific skill tree.
     */
    @Nullable
    private final SkillTree defaultSkillTree;

    public SkillTreeViewer() {
        super("skill-tree");
        this.defaultSkillTree = null;
    }

    public SkillTreeViewer(SkillTree initialSkillTree, boolean isDefault) {
        super("specific-skill-tree-" + (isDefault ? "default" : UtilityMethods.ymlName(initialSkillTree.getId())));
        this.defaultSkillTree = initialSkillTree;
    }

    @Override
    public void reload(@NotNull JavaPlugin plugin, @NotNull ConfigurationSection config) {
        super.reload(plugin, config);

        if (config.contains("status-names"))
            for (NodeState nodeState : NodeState.values())
                statusNames.put(nodeState, config.getString("status-names." + UtilityMethods.ymlName(nodeState.name()), nodeState.name()));

        // Loads all the pathDisplayInfo
        for (NodeState status : NodeState.values())
            for (PathType pathType : PathType.values()) {
                final String configPath = "display.paths." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(pathType.name());
                if (!config.contains(configPath)) {
                    MMOCore.log(Level.WARNING, "An error occurred while loading skill tree GUI: Missing path type: " + MMOCoreUtils.ymlName(pathType.name()) + " for status: " + MMOCoreUtils.ymlName(status.name()));
                    continue;
                }
                icons.put(new PathDisplayInfo(pathType, status), Icon.from(config.get(configPath)));
            }

        // Loads all the nodeDisplayInfo
        for (NodeState status : NodeState.values())
            for (NodeType nodeType : NodeType.values()) {
                final String configPath = "display.nodes." + MMOCoreUtils.ymlName(status.name()) + "." + MMOCoreUtils.ymlName(nodeType.name());
                if (!config.contains(configPath)) {
                    MMOCore.log(Level.WARNING, "An error occurred while loading skill tree GUI: Missing node type: " + MMOCoreUtils.ymlName(nodeType.name()) + " for status: " + MMOCoreUtils.ymlName(status.name()));
                    continue;
                }
                icons.put(new NodeDisplayInfo(nodeType, status), Icon.from(config.get(configPath)));
            }
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equals("skill-tree")) return new SkillTreeItem(config);

        if (function.equals("up")) return new UpArrow(config);
        if (function.equals("left")) return new LeftArrow(config);
        if (function.equals("down")) return new DownArrow(config);
        if (function.equals("right")) return new RightArrow(config);

        if (function.equals("reallocation")) return new ReallocateButton(config);

        if (function.equals("skill-tree-node")) return new SkillTreeNodeItem(config);

        if (function.equals("next-tree-list-page")) return new NextTreeListPageItem(config);
        if (function.equals("previous-tree-list-page")) return new PreviousTreeListPageItem(config);

        return null;
    }

    public SkillTreeInventory newInventory(PlayerData playerData) {
        return new SkillTreeInventory(playerData, defaultSkillTree);
    }

    public class ReallocateButton extends PhysicalItem<SkillTreeInventory> {
        public ReallocateButton(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("skill-tree-points", inv.playerData.getSkillTreePoints(inv.getSkillTree().getId()));
            holders.register("global-points", inv.playerData.getSkillTreePoints("global"));
            holders.register("realloc-points", inv.playerData.getSkillTreeReallocationPoints());
            int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
            holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
            holders.register("point-spent", inv.playerData.getPointsSpent(inv.getSkillTree()));

            return holders;
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {

            int spent = inv.playerData.getPointsSpent(inv.skillTree);
            if (spent < 1) {
                ConfigMessage.fromKey("no-skill-tree-points-spent").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            if (inv.playerData.getSkillTreeReallocationPoints() <= 0) {
                ConfigMessage.fromKey("not-skill-tree-reallocation-point").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            int reallocated = inv.playerData.getPointsSpent(inv.skillTree);
            //We remove all the nodeStates progress
            inv.playerData.giveSkillTreePoints(inv.skillTree.getId(), reallocated);
            inv.playerData.giveSkillTreeReallocationPoints(-1);
            inv.playerData.resetSkillTree(inv.skillTree);
            inv.skillTree.setupNodeStates(inv.playerData);
            ConfigMessage.fromKey("reallocated-points", "points", inv.playerData.getSkillTreePoints(inv.skillTree.getId()), "skill-tree", inv.skillTree.getName()).send(inv.playerData);
            MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_SKILL_TREE).playTo(inv.getPlayer());
            inv.open();
        }
    }

    public class UpArrow extends SimpleItem<SkillTreeInventory> {
        public UpArrow(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.y -= MMOCore.plugin.configManager.skillTreeScrollStepY;
            inv.open();
        }
    }

    public class DownArrow extends SimpleItem<SkillTreeInventory> {
        public DownArrow(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.y += MMOCore.plugin.configManager.skillTreeScrollStepY;
            inv.open();
        }
    }

    public class LeftArrow extends SimpleItem<SkillTreeInventory> {
        public LeftArrow(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.x -= MMOCore.plugin.configManager.skillTreeScrollStepX;
            inv.open();
        }
    }

    public class RightArrow extends SimpleItem<SkillTreeInventory> {
        public RightArrow(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.x += MMOCore.plugin.configManager.skillTreeScrollStepX;
            inv.open();
        }
    }

    public class SkillTreeItem extends PhysicalItem<SkillTreeInventory> {
        public SkillTreeItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(SkillTreeInventory inv, int n) {
            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            if (inv.skillTrees.size() <= index) return null;

            SkillTree skillTree = inv.skillTrees.get(index);
            //We display with the material corresponding to the skillTree
            ItemStack item = super.getDisplayedItem(inv, ItemOptions.material(n, skillTree.getItem()));

            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setDisplayName(skillTree.getName());
            Placeholders holders = getPlaceholders(inv, n);
            List<String> lore = new ArrayList<>();
            meta.getLore().forEach(string -> {
                if (string.contains("{tree-lore}")) {
                    lore.addAll(skillTree.getLore());
                } else
                    lore.add(holders.apply(inv.getPlayer(), string));
            });
            meta.setLore(lore);
            meta.setCustomModelData(skillTree.getCustomModelData());
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING, skillTree.getId());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            SkillTree skillTree = inv.skillTrees.get(index);
            Placeholders holders = new Placeholders();
            holders.register("name", skillTree.getName());
            holders.register("id", skillTree.getId());
            int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
            holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
            holders.register("point-spent", inv.playerData.getPointsSpent(inv.getSkillTree()));
            holders.register("skill-tree-points", inv.playerData.getSkillTreePoints(inv.getSkillTree().getId()));
            holders.register("global-points", inv.playerData.getSkillTreePoints("global"));
            return holders;
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            String id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(
                    new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING);
            MMOCore.plugin.soundManager.getSound(SoundEvent.CHANGE_SKILL_TREE).playTo(inv.getPlayer());
            inv.skillTree = MMOCore.plugin.skillTreeManager.get(id);
            inv.open();
        }
    }

    public class NextTreeListPageItem extends SimpleItem<SkillTreeInventory> {
        public NextTreeListPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.treeListPage++;
            inv.open();
        }

        @Override
        public boolean isDisplayed(SkillTreeInventory inv) {
            return inv.getTreeListPage() < inv.getMaxTreeListPage();
        }
    }

    public class PreviousTreeListPageItem extends SimpleItem<SkillTreeInventory> {

        public PreviousTreeListPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            inv.treeListPage--;
            inv.open();
        }

        @Override
        public boolean isDisplayed(SkillTreeInventory inv) {
            return inv.getTreeListPage() > 0;
        }
    }

    public class SkillTreeNodeItem extends PhysicalItem<SkillTreeInventory> {
        private final List<String> pathLore = new ArrayList<>();

        public SkillTreeNodeItem(ConfigurationSection config) {
            super(config);

            if (config.isList("path-lore"))
                pathLore.addAll(config.getStringList("path-lore"));
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        /**
         * Display the node/path with the lore and name filled in the yml of the skill tree node with the right material
         * and model-data.
         * You don't need to give any name or lore in the gui/skilltree.yml all the information are filled in
         * the yml of the skill tree.
         */
        @Override
        public ItemStack getDisplayedItem(SkillTreeInventory inv, int n) {
            IntegerCoordinates coordinates = inv.getCoordinates(n);
            if (inv.getSkillTree().isPathOrNode(coordinates)) {
                Icon icon = inv.getIcon(coordinates);
                ItemStack item = super.getDisplayedItem(inv, icon.toItemOptions(n));
                ItemMeta meta = item.getItemMeta();
                Placeholders holders = getPlaceholders(inv, n);
                if (inv.getSkillTree().isNode(coordinates)) {
                    SkillTreeNode node = inv.getSkillTree().getNode(coordinates);
                    List<String> lore = new ArrayList<>();
                    meta.getLore().forEach(str -> {
                        if (str.contains("{node-lore}")) {
                            node.getLore(inv.playerData).forEach(s -> lore.add(holders.apply(inv.getPlayer(), str.replace("{node-lore}", s))));
                        } else if (str.contains("{strong-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.STRONG)));
                        } else if (str.contains("{soft-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.SOFT)));
                        } else if (str.contains("{incompatible-parents}")) {
                            lore.addAll(getParentsLore(inv, node, node.getParents(ParentType.INCOMPATIBLE)));
                        } else
                            lore.add(holders.apply(inv.getPlayer(), str));
                    });
                    meta.setLore(lore);
                    final String name = meta.getDisplayName();
                    meta.setDisplayName(name == null || name.isEmpty() ? node.getName() : name);
                }
                //If it is path we remove the display name and the lore.
                else {
                    meta.setLore(pathLore.stream().map(str -> holders.apply(inv.getPlayer(), str)).collect(Collectors.toList()));
                    meta.setDisplayName(" ");
                }
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER, coordinates.getX());
                container.set(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER, coordinates.getY());
                item.setItemMeta(meta);
                return item;
            }
            return new ItemStack(Material.AIR);
        }

        /**
         * Soft&Strong children lore for the node
         */
        public List<String> getParentsLore(SkillTreeInventory inv, SkillTreeNode node, Collection<SkillTreeNode> parents) {
            List<String> lore = new ArrayList<>();
            for (SkillTreeNode parent : parents) {
                int level = inv.playerData.getNodeLevel(parent);
                ChatColor color = level >= node.getParentNeededLevel(parent) ? ChatColor.GREEN : ChatColor.RED;
                lore.add(ChatColor.GRAY + "◆" + parent.getName() + ": " + color + node.getParentNeededLevel(parent));
            }
            return lore;
        }

        @Override
        public Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("skill-tree", inv.getSkillTree().getName());
            boolean isNode = inv.getSkillTree().isNode(inv.getCoordinates(n));
            if (isNode) {
                SkillTreeNode node = inv.getNode(n);
                holders.register("current-level", inv.playerData.getNodeLevel(node));
                NodeState status = inv.playerData.getNodeState(node);
                holders.register("current-state", statusNames.getOrDefault(status, status.name()));
                holders.register("max-level", node.getMaxLevel());
                holders.register("name", node.getName());
                holders.register("max-children", node.getMaxChildren());
                holders.register("point-consumed", node.getPointConsumption());
                holders.register("display-type", node.getNodeType());
            } else {
                holders.register("display-type", inv.skillTree.getPath(inv.getCoordinates(n)).getPathType());
            }
            int maxPointSpent = inv.getSkillTree().getMaxPointSpent();
            holders.register("max-point-spent", maxPointSpent == Integer.MAX_VALUE ? "∞" : maxPointSpent);
            holders.register("point-spent", inv.playerData.getPointsSpent(inv.getSkillTree()));
            holders.register("skill-tree-points", inv.playerData.getSkillTreePoints(inv.getSkillTree().getId()));
            holders.register("global-points", inv.playerData.getSkillTreePoints("global"));

            return holders;
        }

        @Override
        public void onClick(@NotNull SkillTreeInventory inv, @NotNull InventoryClickEvent event) {
            if (event.getClick() != ClickType.LEFT) return;

            final PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
            final int x = container.get(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER);
            final int y = container.get(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER);
            if (!inv.skillTree.isNode(new IntegerCoordinates(x, y))) return;

            // Maximum amount of skill points spent in node
            final SkillTreeNode node = inv.skillTree.getNode(new IntegerCoordinates(x, y));
            if (inv.playerData.getPointsSpent(inv.skillTree) >= inv.skillTree.getMaxPointSpent()) {
                ConfigMessage.fromKey("max-points-reached").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            switch (inv.playerData.canIncrementNodeLevel(node)) {
                case SUCCESS: {
                    inv.playerData.incrementNodeLevel(node);
                    ConfigMessage.fromKey("upgrade-skill-node", "skill-node", node.getName(), "level", inv.playerData.getNodeLevel(node)).send(inv.playerData);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_SKILL_TREE_NODE).playTo(inv.getPlayer());
                    inv.open();
                    break;
                }

                case PERMISSION_DENIED: {
                    ConfigMessage.fromKey("missing-skill-node-permission").send(inv.playerData);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                    break;
                }

                case LOCKED_NODE: {
                    ConfigMessage.fromKey("locked-node").send(inv.playerData);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                    break;
                }

                case MAX_LEVEL_REACHED: {
                    ConfigMessage.fromKey("skill-node-max-level-hit").send(inv.playerData);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                    break;
                }

                case NOT_ENOUGH_POINTS: {
                    ConfigMessage.fromKey("not-enough-skill-tree-points", "point", node.getPointConsumption()).send(inv.playerData);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                    break;
                }
            }
        }
    }

    public class SkillTreeInventory extends GeneratedInventory {
        private final int width, height;
        private final int maxTreeListPage;
        private final List<SkillTree> skillTrees;
        private final List<Integer> slots;
        private final PlayerData playerData;

        @NotNull
        private SkillTree skillTree;
        private int treeListPage;
        private int x, y;
        //width and height correspond to the the size of the 'board' representing the skill tree
        private int minSlot, maxSlot;

        public SkillTreeInventory(PlayerData playerData, SkillTree skillTree) {
            super(new Navigator(playerData.getMMOPlayerData()), SkillTreeViewer.this);

            this.playerData = playerData;
            skillTrees = playerData.getProfess().getSkillTrees();
            this.skillTree = skillTree == null ? skillTrees.get(0) : skillTree;
            if (skillTree == null)
                maxTreeListPage = (skillTrees.size() - 1) / SkillTreeViewer.this.getByFunction("skill-tree").getSlots().size();
            else
                maxTreeListPage = 0;
            //We get the width and height of the GUI(corresponding to the slots given)
            slots = SkillTreeViewer.this.getByFunction("skill-tree-node").getSlots();
            minSlot = 64;
            maxSlot = 0;
            for (int slot : slots) {
                if (slot < minSlot)
                    minSlot = slot;
                if (slot > maxSlot)
                    maxSlot = slot;
            }
            width = (maxSlot - minSlot) % 9;
            height = (maxSlot - minSlot) / 9;
            x -= width / 2;
            y -= height / 2;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getTreeListPage() {
            return treeListPage;
        }

        public int getMaxTreeListPage() {
            return maxTreeListPage;
        }

        public PlayerData getPlayerData() {
            return playerData;
        }

        public Icon getIcon(IntegerCoordinates coordinates) {
            if (skillTree.isNode(coordinates)) {
                SkillTreeNode node = skillTree.getNode(coordinates);
                NodeType nodeType = node.getNodeType();
                NodeState nodeState = playerData.getNodeState(node);
                //If the node has its own display, it will be shown.
                if (node.hasIcon(nodeState))
                    return node.getIcon(nodeState);
                DisplayInfo displayInfo = new NodeDisplayInfo(nodeType, nodeState);
                //Takes the display defined in the skill tree config if it exists.
                if (skillTree.hasIcon(displayInfo))
                    return skillTree.getIcon(displayInfo);

                Icon icon = icons.get(displayInfo);
                Validate.notNull(icon, "The node " + node.getFullId() + " has no icon for the type " + nodeType + " and the status " + nodeState);
                return icon;
            } else {
                SkillTreePath path = skillTree.getPath(coordinates);
                PathType pathType = path.getPathType();
                NodeState pathStatus = path.getStatus(playerData);
                DisplayInfo displayInfo = new PathDisplayInfo(pathType, pathStatus);
                //Takes the display defined in the skill tree config if it exists.
                if (skillTree.hasIcon(displayInfo))
                    return skillTree.getIcon(displayInfo);
                Icon icon = icons.get(displayInfo);
                Validate.notNull(icon, "There is no icon for the path type " + pathType + " and the status " + pathStatus);
                return icon;
            }
        }

        @NotNull
        @Override
        public String getRawName() {
            return guiName.replace("{skill-tree-name}", skillTree.getName()).replace("{skill-tree-id}", skillTree.getId());
        }

        public IntegerCoordinates getCoordinates(int n) {
            int slot = slots.get(n);
            int deltaX = (slot - minSlot) % 9;
            int deltaY = (slot - minSlot) / 9;
            IntegerCoordinates coordinates = new IntegerCoordinates(getX() + deltaX, getY() + deltaY);
            return coordinates;
        }

        public SkillTreeNode getNode(int n) {
            return getSkillTree().getNode(getCoordinates(n));
        }

        public SkillTree getSkillTree() {
            return skillTree;
        }

        public int getMinSlot() {
            return minSlot;
        }
    }
}
