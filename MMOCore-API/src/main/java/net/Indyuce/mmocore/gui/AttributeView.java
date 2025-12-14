package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerAttributeUseEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeView extends EditableInventory {
    public AttributeView() {
        super("attribute-view");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("reallocation")) return new ReallocateButton(config);
        if (function.startsWith("attribute_")) return new AttributeItem(function, config);

        return null;
    }

    public AttrInventory newInventory(PlayerData data) {
        return new AttrInventory(data);
    }

    public class ReallocateButton extends PhysicalItem<AttrInventory> {
        public ReallocateButton(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(AttrInventory inv, int n) {
            Placeholders holders = new Placeholders();
            holders.register("attribute_points", inv.playerData.getAttributePoints());
            holders.register("points", inv.playerData.getAttributeReallocationPoints());
            holders.register("total", inv.playerData.getAttributes().countPoints());
            return holders;
        }

        @Override
        public void onClick(@NotNull AttrInventory inv, @NotNull InventoryClickEvent event) {
            int spent = inv.playerData.getAttributes().countPoints();
            if (spent < 1) {
                Message.ATTRIBUTE_NO_POINTS_SPENT.send(inv.playerData);
                return;
            }

            if (inv.playerData.getAttributeReallocationPoints() < 1) {
                Message.ATTRIBUTE_MISSING_REALLOCATION_POINT.send(inv.playerData);
                return;
            }

            inv.playerData.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
            inv.playerData.giveAttributePoints(spent);
            inv.playerData.giveAttributeReallocationPoints(-1);
            Message.ATTRIBUTE_POINTS_REALLOCATED.send(inv.playerData, "points", inv.playerData.getAttributePoints());
            inv.open();
        }
    }

    public static class AttributeItem extends PhysicalItem<AttrInventory> {
        private final PlayerAttribute attribute;
        private final int shiftCost;

        public AttributeItem(String function, ConfigurationSection config) {
            super(config);

            attribute = MMOCore.plugin.attributeManager
                    .get(function.substring("attribute_".length()).toLowerCase().replace(" ", "-").replace("_", "-"));
            shiftCost = Math.max(config.getInt("shift-cost"), 1);
        }

        @Override
        public Placeholders getPlaceholders(AttrInventory inv, int n) {
            int total = inv.playerData.getAttributes().getInstance(attribute).getTotal();
            final var attrInst = inv.playerData.getAttributes().getInstance(attribute);

            Placeholders holders = new Placeholders();
            holders.register("name", attribute.getName());
            holders.register("buffs", attribute.getBuffs().size());
            holders.register("spent", attrInst.getBase());
            holders.register("total", attrInst.getTotal());
            holders.register("max", attribute.getMax());
            holders.register("current", total);
            holders.register("attribute_points", inv.playerData.getAttributePoints());
            holders.register("shift_points", shiftCost);
            attribute.getBuffs().forEach(buff -> {
                final String stat = buff.getStat();
                holders.register("buff_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.getValue()));
                holders.register("total_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.multiply(total).getValue()));
            });

            return holders;
        }

        @Override
        public void onClick(@NotNull AttrInventory inv, @NotNull InventoryClickEvent event) {

            if (inv.playerData.getAttributePoints() < 1) {
                Message.ATTRIBUTE_MISSING_POINT.send(inv.playerData);
                return;
            }

            PlayerAttributes.AttributeInstance ins = inv.playerData.getAttributes().getInstance(attribute);
            if (attribute.hasMax() && ins.getBase() >= attribute.getMax()) {
                Message.ATTRIBUTE_MAX_POINTS_HIT.send(inv.playerData);
                return;
            }

            // Amount of points spent
            final boolean shiftClick = event.getClick().isShiftClick();
            int pointsSpent = shiftClick ? shiftCost : 1;
            if (attribute.hasMax()) pointsSpent = Math.min(pointsSpent, attribute.getMax() - ins.getBase());

            if (shiftClick && inv.playerData.getAttributePoints() < pointsSpent) {
                Message.ATTRIBUTE_MISSING_POINT_SHIFT.send(inv.playerData, "shift_points", pointsSpent);
                return;
            }

            ins.addBase(pointsSpent);
            inv.playerData.giveAttributePoints(-pointsSpent);

            // Apply exp table as many times as required
            while (pointsSpent-- > 0)
                attribute.updateAdvancement(inv.playerData, ins.getBase());

            Message.ATTRIBUTE_LEVEL_UP.send(inv.playerData, "attribute", attribute.getName(), "level", ins.getBase());
            var calledEvent = new PlayerAttributeUseEvent(inv.playerData, attribute);
            Bukkit.getServer().getPluginManager().callEvent(calledEvent);
            inv.open();
        }
    }

    public class AttrInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public AttrInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), AttributeView.this);

            this.playerData = playerData;
        }
    }
}