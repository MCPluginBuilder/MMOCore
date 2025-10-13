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
import io.lumine.mythic.lib.gui.util.IconOptions;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Message;
import net.Indyuce.mmocore.skilltree.IntCoords;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.Indyuce.mmocore.skilltree.ParentType;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.display.DisplayMap;
import net.Indyuce.mmocore.skilltree.display.NodeDisplayInfo;
import net.Indyuce.mmocore.skilltree.display.PathDisplayInfo;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.bukkit.ChatColor;
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
import java.util.concurrent.atomic.AtomicInteger;

public class SkillTreeViewer extends EditableInventory {
    protected DisplayMap icons;
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

        if (config.contains("status-names")) for (NodeState nodeState : NodeState.values())
            statusNames.put(nodeState, config.getString("status-names." + UtilityMethods.ymlName(nodeState.name()), nodeState.name()));

        // Loads display info
        icons = DisplayMap.from(config.getConfigurationSection("display"));
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
            holders.register("skill-tree-points", inv.playerData.getSkillTreePoints(inv.getSkillTree()));
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
                Message.NO_SKILL_TREE_POINTS_SPENT.send(inv.playerData);
                return;
            }

            if (inv.playerData.getSkillTreeReallocationPoints() <= 0) {
                Message.NOT_SKILL_TREE_REALLOCATION_POINT.send(inv.playerData);
                return;
            }

            int reallocated = inv.playerData.getPointsSpent(inv.skillTree);
            inv.playerData.giveSkillTreePoints(inv.skillTree.getId(), reallocated);
            inv.playerData.giveSkillTreeReallocationPoints(-1);
            inv.playerData.resetSkillTree(inv.skillTree);
            inv.skillTree.resolveStates(inv.playerData);
            Message.SKILL_TREE_REALLOCATE.send(inv.playerData, "points", inv.playerData.getSkillTreePoints(inv.skillTree.getId()), "skill-tree", inv.skillTree.getName());
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
        public void preprocessLore(@NotNull SkillTreeInventory inv, int n, @NotNull List<String> lore) {

            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            if (inv.skillTrees.size() <= index) return;
            SkillTree skillTree = inv.skillTrees.get(index);

            var loreIdx = lore.indexOf("{tree-lore}");
            if (loreIdx != -1) {
                lore.remove(loreIdx);
                lore.addAll(loreIdx, skillTree.getLore());
            }
        }

        @Override
        public ItemStack getDisplayedItem(SkillTreeInventory inv, int n) {
            int index = inv.getEditable().getByFunction("skill-tree").getSlots().size() * inv.treeListPage + n;
            if (inv.skillTrees.size() <= index) return null;

            SkillTree skillTree = inv.skillTrees.get(index);
            //We display with the material corresponding to the skillTree
            ItemStack item = super.getDisplayedItem(inv, ItemOptions.material(n, skillTree.getItem()));

            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Hardcode 'hide-flags' on
            meta.setDisplayName(skillTree.getName());
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
            String id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(MMOCore.plugin, "skill-tree-id"), PersistentDataType.STRING);
            var skillTree = Objects.requireNonNull(MMOCore.plugin.skillTreeManager.get(id), "No skill tree found with ID " + id);
            Message.SKILL_TREE_SWITCH.send(inv.playerData, "skill-tree", skillTree.getName());
            inv.skillTree = skillTree;
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

            if (config.isList("path-lore")) pathLore.addAll(config.getStringList("path-lore"));
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void preprocessLore(@NotNull SkillTreeInventory inv, int n, @NotNull List<String> lore) {
            IntCoords coordinates = inv.getCoordinates(n);
            if (!inv.getSkillTree().isNode(coordinates)) return;

            SkillTreeNode node = inv.getSkillTree().getNode(coordinates);

            for (int i = 0; i < lore.size(); ) {
                String str = lore.get(i);
                if (str.contains("{node-lore}")) {
                    lore.remove(i);
                    List<String> shaded = node.getLore(inv.playerData);
                    var _i = new AtomicInteger(i);
                    shaded.forEach(s -> lore.add(_i.getAndIncrement(), str.replace("{node-lore}", s)));
                    i += shaded.size();
                } else if (str.contains("{strong-parents}")) {
                    lore.remove(i);
                    List<String> shaded = getParentsLore(inv, node, node.getParents(ParentType.STRONG));
                    lore.addAll(i, shaded);
                    i += shaded.size();
                } else if (str.contains("{soft-parents}")) {
                    lore.remove(i);
                    List<String> shaded = getParentsLore(inv, node, node.getParents(ParentType.SOFT));
                    lore.addAll(i, shaded);
                    i += shaded.size();
                } else if (str.contains("{incompatible-parents}")) {
                    lore.remove(i);
                    List<String> shaded = getParentsLore(inv, node, node.getParents(ParentType.INCOMPATIBLE));
                    lore.addAll(i, shaded);
                    i += shaded.size();
                } else {
                    i++;
                }
            }
        }

        /**
         * Display the node/path with the lore and name filled in the yml of the skill tree node with the right material
         * and model-data.
         * You don't need to give any name or lore in the gui/skilltree.yml all the information are filled in
         * the yml of the skill tree.
         */
        @Override
        public ItemStack getDisplayedItem(SkillTreeInventory inv, int n) {
            IntCoords coordinates = inv.getCoordinates(n);

            IconOptions icon = inv.computeIcon(coordinates);
            if (icon == null) return null; // Neither a path nor a node

            ItemStack item = super.getDisplayedItem(inv, new ItemOptions(n, icon));
            ItemMeta meta = item.getItemMeta();

            // Make sure name is not null
            if (inv.getSkillTree().isNode(coordinates)) {
                SkillTreeNode node = inv.getSkillTree().getNode(coordinates);
                if (!meta.hasDisplayName() || meta.getDisplayName().isEmpty()) meta.setDisplayName(node.getName());
            }

            // If it is path we remove the display name and the lore.
            else {
                meta.setLore(new ArrayList<>(pathLore));
                meta.setDisplayName(" ");
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // Hardcode hide-flags to 'true'
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "coordinates.x"), PersistentDataType.INTEGER, coordinates.getX());
            container.set(new NamespacedKey(MMOCore.plugin, "coordinates.y"), PersistentDataType.INTEGER, coordinates.getY());
            item.setItemMeta(meta);
            return item;
        }

        /**
         * Soft&Strong children lore for the node
         */
        public List<String> getParentsLore(SkillTreeInventory inv, SkillTreeNode node, Collection<SkillTreeNode> parents) {
            // TODO why is this hardcoded >:(
            List<String> lore = new ArrayList<>();
            for (SkillTreeNode parent : parents) {
                int level = inv.playerData.getNodeLevel(parent);
                ChatColor color = level >= node.getParentNeededLevel(parent) ? ChatColor.GREEN : ChatColor.RED;
                lore.add(ChatColor.GRAY + "◆" + parent.getName() + ": " + color + node.getParentNeededLevel(parent));
            }
            return lore;
        }

        @Override
        public @NotNull Placeholders getPlaceholders(SkillTreeInventory inv, int n) {
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
                //holders.register("display-type", node.getNodeType());
            } /*else {
                holders.register("display-type", inv.skillTree.getPath(inv.getCoordinates(n)).getPathType());
            }*/
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
            if (!inv.skillTree.isNode(new IntCoords(x, y))) return;

            // Higher number of points spent in SKILL TREE (not node)
            final SkillTreeNode node = inv.skillTree.getNode(new IntCoords(x, y));
            if (inv.playerData.getPointsSpent(inv.skillTree) >= inv.skillTree.getMaxPointSpent()) {
                Message.SKILL_TREE_MAX_POINTS_SPENT.send(inv.playerData);
                return;
            }

            switch (inv.playerData.canIncrementNodeLevel(node)) {
                case SUCCESS: {
                    inv.playerData.incrementNodeLevel(node);
                    Message.SKILL_TREE_UPGRADE_NODE.send(inv.playerData, "skill-node", node.getName(), "level", inv.playerData.getNodeLevel(node));
                    inv.open();
                    break;
                }

                case PERMISSION_DENIED: {
                    Message.MISSING_SKILL_NODE_PERMISSION.send(inv.playerData);
                    break;
                }

                case LOCKED_NODE: {
                    Message.SKILL_TREE_NODE_LOCKED.send(inv.playerData);
                    break;
                }

                // Max number of points spent in that NODE (not skill tree)
                case MAX_LEVEL_REACHED: {
                    Message.SKILL_TREE_NODE_MAX_LEVEL_HIT.send(inv.playerData);
                    break;
                }

                case NOT_ENOUGH_POINTS: {
                    Message.NOT_ENOUGH_SKILL_TREE_POINTS.send(inv.playerData, "point", node.getPointConsumption());
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
            else maxTreeListPage = 0;
            //We get the width and height of the GUI(corresponding to the slots given)
            slots = SkillTreeViewer.this.getByFunction("skill-tree-node").getSlots();
            minSlot = 64;
            maxSlot = 0;
            for (int slot : slots) {
                if (slot < minSlot) minSlot = slot;
                if (slot > maxSlot) maxSlot = slot;
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

        @Deprecated
        public IconOptions getIcon(IntCoords coordinates) {
            return computeIcon(coordinates);
        }

        @Nullable
        public IconOptions computeIcon(@NotNull IntCoords coordinates) {

            // Is this a node?
            final var node = skillTree.getNodeOrNull(coordinates);
            if (node != null) {
                final var nodeShape = skillTree.getNodeShape(node);
                final var nodeState = playerData.getNodeState(node);
                var displayInfo = new NodeDisplayInfo(nodeShape, nodeState);

                // Node > skill tree > skill tree UI
                var icon = DisplayMap.getIcon(displayInfo, node.getIcons(), skillTree.getIcons(), icons);
                if (icon == null && nodeState == NodeState.MAXED_OUT) {
                    // Fallback to UNLOCKED if no MAXED_OUT icon found
                    displayInfo = new NodeDisplayInfo(nodeShape, NodeState.UNLOCKED);
                    icon = DisplayMap.getIcon(displayInfo, node.getIcons(), skillTree.getIcons(), icons);
                }
                if (icon == null) icon = DisplayMap.DEFAULT_ICON;
                //Validate.notNull(icon, "Node " + node.getFullId() + " has no icon for shape " + nodeShape + " and state " + nodeState);

                return icon;
            }

            final var edge = skillTree.getPath(coordinates);
            if (edge != null) {
                final var pathState = playerData.getPathState(edge);
                final var pathShape = edge.getShape(coordinates);
                final var displayInfo = new PathDisplayInfo(pathShape, pathState);

                // Skill tree > Skill tree UI
                var icon = DisplayMap.getIcon(displayInfo, skillTree.getIcons(), icons);
                if (icon == null) icon = DisplayMap.DEFAULT_ICON;
                //Validate.notNull(icon, "No icon for path shape " + pathShape + " and state " + pathStatus);

                return icon;
            }

            // Neither a node or a path
            return null;
        }

        @NotNull
        @Override
        public String getRawName() {
            return guiName.replace("{skill-tree-name}", skillTree.getName()).replace("{skill-tree-id}", skillTree.getId());
        }

        public IntCoords getCoordinates(int n) {
            int slot = slots.get(n);
            int deltaX = (slot - minSlot) % 9;
            int deltaY = (slot - minSlot) / 9;
            IntCoords coordinates = new IntCoords(getX() + deltaX, getY() + deltaY);
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
