package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.PreviousPageItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import net.Indyuce.mmocore.waypoint.WaypointPathCalculation;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaypointViewer extends EditableInventory {
    public WaypointViewer() {
        super("waypoints");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equals("waypoint")) return new WaypointItem(config);
        if (function.equals("previous")) return new PreviousPageItem<>(config);
        if (function.equals("next")) return new NextPageItem<>(config);

        return null;
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return newInventory(data, null);
    }

    public GeneratedInventory newInventory(PlayerData data, Waypoint waypoint) {
        return new WaypointViewerInventory(data, this, waypoint);
    }

    public class WaypointItem extends InventoryItem<WaypointViewerInventory> {
        private final SimpleItem<WaypointViewerInventory> noWaypoint, locked;
        private final WaypointItemHandler availWaypoint, noStellium, notLinked, currentWayPoint;

        public WaypointItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
            Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
            Validate.notNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config");
            //Validate.notNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config");
            Validate.notNull(config.getConfigurationSection("current-waypoint"), "Could not load 'current-waypoint' config");
            Validate.notNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config");
            Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

            noWaypoint = new SimpleItem<>(config.getConfigurationSection("no-waypoint"));
            locked = new SimpleItem<>(config.getConfigurationSection("locked"));
            notLinked = new WaypointItemHandler(config.getConfigurationSection("not-a-destination"), true);
            //notDynamic = new WaypointItemHandler(config.getConfigurationSection("not-dynamic"), true);
            currentWayPoint = new WaypointItemHandler(config.getConfigurationSection("current-waypoint"), true);
            noStellium = new WaypointItemHandler(config.getConfigurationSection("not-enough-stellium"), false);
            availWaypoint = new WaypointItemHandler(config.getConfigurationSection("display"), false);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(WaypointViewerInventory inv, int n) {

            int index = inv.getPageIndex(n);
            if (index >= inv.waypoints.size())
                return noWaypoint.getDisplayedItem(inv, n);

            final Waypoint waypoint = inv.waypoints.get(index);

            // Current waypoint
            if (inv.current != null && inv.current.equals(waypoint))
                return currentWayPoint.getDisplayedItem(inv, n);

            // Locked waypoint
            if (!inv.playerData.hasWaypoint(waypoint))
                return locked.getDisplayedItem(inv, n);

            // Waypoints are not linked
            if (!inv.paths.containsKey(waypoint))
                return notLinked.getDisplayedItem(inv, n);

            // Normal cost
            if (inv.paths.get(waypoint).getCost() > inv.playerData.getStellium())
                return noStellium.getDisplayedItem(inv, n);

            return availWaypoint.getDisplayedItem(inv, n);
        }
    }

    public class WaypointItemHandler extends PhysicalItem<WaypointViewerInventory> {
        private final boolean onlyName;
        private final String splitter, none;

        public WaypointItemHandler(ConfigurationSection config, boolean onlyName) {
            super(config);

            this.onlyName = onlyName;
            this.splitter = config.getString("format_path.splitter", ", ");
            this.none = config.getString("format_path.none", "None");
        }

        @Override
        public ItemStack getDisplayedItem(WaypointViewerInventory inv, int n) {
            final OfflinePlayer effectivePlayer = getEffectivePlayer(inv, n); // TODO check if this is needed

            final ItemStack item = super.getDisplayedItem(inv, n);
            final ItemMeta meta = item.getItemMeta();
            final Placeholders placeholders = getPlaceholders(inv, n); // TODO remove dupe call

            // If a player can teleport to another waypoint given his location
            Waypoint waypoint = inv.waypoints.get(inv.getPageIndex(n));

            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                meta.getLore().forEach(line -> {
                    if (line.equals("{lore}")) for (String added : waypoint.getLore())
                        lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, added));
                    else lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, line));
                });
                meta.setLore(lore);
            }

            item.setItemMeta(meta);

            // Extra code
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING, waypoint.getId());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public Placeholders getPlaceholders(WaypointViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();

            Waypoint waypoint = inv.waypoints.get(inv.getPageIndex(n));
            holders.register("name", waypoint.getName());

            if (!onlyName) {
                holders.register("current_cost", inv.paths.get(waypoint).getCost());
                holders.register("normal_cost", ONE_DIGIT.format(inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).getCost() : Double.POSITIVE_INFINITY));
                holders.register("dynamic_cost", ONE_DIGIT.format(waypoint.getDynamicCost()));
                holders.register("intermediary_waypoints", inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).displayIntermediaryWayPoints(splitter, none) : none);
            }

            return holders;
        }

        @Override
        public void onClick(@NotNull WaypointViewerInventory inv, @NotNull InventoryClickEvent event) {
            PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
            String tag = container.has(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) ?
                    container.get(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) : "";

            if (tag.isEmpty()) return;

            // Locked waypoint?
            final Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
            if (!inv.playerData.hasWaypoint(waypoint)) {
                ConfigMessage.fromKey("not-unlocked-waypoint").send(inv.playerData);
                return;
            }

            // Cannot teleport to current waypoint
            if (waypoint.equals(inv.current)) {
                ConfigMessage.fromKey("standing-on-waypoint").send(inv.playerData);
                return;
            }

            // No access to that waypoint
            if (inv.paths.get(waypoint) == null) {
                ConfigMessage.fromKey("cannot-teleport-to").send(inv.playerData);
                return;
            }

            // Stellium cost
            double withdraw = inv.paths.get(waypoint).getCost();
            double left = withdraw - inv.playerData.getStellium();
            if (left > 0) {
                ConfigMessage.fromKey("not-enough-stellium", "more", ONE_DIGIT.format(left)).send(inv.playerData);
                return;
            }

            if (inv.playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
                return;

            inv.getPlayer().closeInventory();
            inv.playerData.warp(waypoint, withdraw);
        }
    }

    public class WaypointViewerInventory extends GeneratedInventory {
        private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
        @Nullable
        private final Waypoint current;
        private final PlayerData playerData;

        private Map<Waypoint, WaypointPath> paths;

        public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current) {
            super(new Navigator(playerData.getMMOPlayerData()), editable);

            this.playerData = playerData;
            this.current = current;
            paths = new WaypointPathCalculation(playerData).run(current).getPaths();

            enablePagination(editable.getByFunction("waypoint").getSlots().size());
        }

        @Override
        public int getMaxPage() {
            return computeMaxPage(waypoints.size());
        }

        public boolean isDynamicUse() {
            return current == null;
        }
    }
}
