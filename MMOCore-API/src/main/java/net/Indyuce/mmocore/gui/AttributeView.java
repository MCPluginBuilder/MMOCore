package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerAttributeUseEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;

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
                ConfigMessage.fromKey("no-attribute-points-spent").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            if (inv.playerData.getAttributeReallocationPoints() < 1) {
                ConfigMessage.fromKey("not-attribute-reallocation-point").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            inv.playerData.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
            inv.playerData.giveAttributePoints(spent);
            inv.playerData.giveAttributeReallocationPoints(-1);
            ConfigMessage.fromKey("attribute-points-reallocated", "points", inv.playerData.getAttributePoints()).send(inv.playerData);
            MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_ATTRIBUTES).playTo(inv.getPlayer());
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

            Placeholders holders = new Placeholders();
            holders.register("name", attribute.getName());
            holders.register("buffs", attribute.getBuffs().size());
            holders.register("spent", inv.playerData.getAttributes().getInstance(attribute).getBase());
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
                ConfigMessage.fromKey("not-attribute-point").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            PlayerAttributes.AttributeInstance ins = inv.playerData.getAttributes().getInstance(attribute);
            if (attribute.hasMax() && ins.getBase() >= attribute.getMax()) {
                ConfigMessage.fromKey("attribute-max-points-hit").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            // Amount of points spent
            final boolean shiftClick = event.getClick().isShiftClick();
            int pointsSpent = shiftClick ? shiftCost : 1;
            if (attribute.hasMax()) pointsSpent = Math.min(pointsSpent, attribute.getMax() - ins.getBase());

            if (shiftClick && inv.playerData.getAttributePoints() < pointsSpent) {
                ConfigMessage.fromKey("not-attribute-point-shift", "shift_points", String.valueOf(pointsSpent)).send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            ins.addBase(pointsSpent);
            inv.playerData.giveAttributePoints(-pointsSpent);

            // Apply exp table as many times as required
            while (pointsSpent-- > 0)
                attribute.updateAdvancement(inv.playerData, ins.getBase());

            ConfigMessage.fromKey("attribute-level-up", "attribute", attribute.getName(), "level", String.valueOf(ins.getBase())).send(inv.playerData);
            MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_ATTRIBUTE).playTo(inv.getPlayer());

            PlayerAttributeUseEvent playerAttributeUseEvent = new PlayerAttributeUseEvent(inv.playerData, attribute);
            Bukkit.getServer().getPluginManager().callEvent(playerAttributeUseEvent);

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