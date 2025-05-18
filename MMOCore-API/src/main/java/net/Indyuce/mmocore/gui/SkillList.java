package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.PreviousPageItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SkillList extends EditableInventory {
    public SkillList() {
        super("skill-list");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        switch (function) {
            case "skill":
                return new SkillItem(config);
            case "level":
                return new LevelItem(config);
            case "upgrade":
                return new UpgradeItem(config);
            case "reallocation":
                return new ReallocationItem(config);
            case "slot":
                return new SlotItem(config);
            case "previous":
                return new PreviousPageItem<>(config);
            case "next":
                return new NextPageItem<>(config);
            case "selected":
                return new SelectedItem(config);
            default:
                return null;
        }
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new SkillViewerInventory(data);
    }

    public class ReallocationItem extends PhysicalItem<SkillViewerInventory> {
        public ReallocationItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public @NotNull Placeholders getPlaceholders(SkillViewerInventory inv, int i) {
            Placeholders holders = new Placeholders();
            holders.register("skill_points", inv.playerData.getSkillPoints());
            holders.register("points", inv.playerData.getSkillReallocationPoints());
            holders.register("total", inv.playerData.countSkillPointsSpent());
            return holders;
        }

        @Override
        public void onClick(@NotNull SkillViewerInventory inv, @NotNull InventoryClickEvent event) {
            int spent = inv.playerData.countSkillPointsSpent();

            if (spent < 1) {
                ConfigMessage.fromKey("no-skill-points-spent").send(inv.getPlayer());
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            if (inv.playerData.getSkillReallocationPoints() < 1) {
                ConfigMessage.fromKey("not-skill-reallocation-point").send(inv.playerData);
                MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(inv.getPlayer());
                return;
            }

            for (ClassSkill skill : inv.playerData.getProfess().getSkills())
                inv.playerData.setSkillLevel(skill.getSkill(), 1);

            inv.playerData.giveSkillPoints(spent);
            inv.playerData.setSkillReallocationPoints(inv.playerData.getSkillReallocationPoints() - 1);
            ConfigMessage.fromKey("skill-points-reallocated", "points", inv.playerData.getSkillPoints()).send(inv.getPlayer());
            MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_SKILLS).playTo(inv.getPlayer());
            inv.open();
        }
    }

    public class SelectedItem extends PhysicalItem<SkillViewerInventory> {
        public SelectedItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void preprocessLore(@NotNull SkillViewerInventory inv, int index, @NotNull List<String> lore) {
            if (inv.selected == null) return;

            int skillLevel = inv.playerData.getSkillLevel(inv.selected.getSkill());
            boolean unlocked = inv.selected.getUnlockLevel() <= inv.playerData.getLevel();

            // Replace skill lore
            int loreIdx = lore.indexOf("{lore}");
            if (loreIdx >= 0) {
                lore.remove(loreIdx);
                lore.addAll(loreIdx, inv.selected.calculateLore(inv.playerData));
            }

            // Remove condition placeholders
            for (var i = 0; i < lore.size(); ) {
                String str = lore.get(i);
                if (str.startsWith("{unlocked}")) {
                    if (!unlocked) lore.remove(i);
                    else lore.set(i, str.substring("{unlocked}".length()));
                } else if (str.startsWith("{locked}")) {
                    if (unlocked) lore.remove(i);
                    else lore.set(i, str.substring("{locked}".length()));
                } else if (str.startsWith("{max_level}")) {
                    if (!inv.selected.hasMaxLevel() || inv.selected.getMaxLevel() > inv.playerData.getSkillLevel(inv.selected.getSkill()))
                        lore.remove(i);
                    else lore.set(i, str.substring("{max_level}".length()));
                } else i++;
            }
        }

        public ItemStack getDisplayedItem(SkillViewerInventory inv, int n) {
            if (inv.selected == null) return new ItemStack(Material.AIR);

            return getDisplayedItem(inv, inv.selected.getSkill().getRawIcon().toItemOptions(n));
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            var skillLevel = inv.playerData.getSkillLevel(inv.selected.getSkill());

            Placeholders holders = new Placeholders();
            holders.register("selected", inv.selected.getSkill().getName());
            holders.register("skill", inv.selected.getSkill().getName());
            holders.register("unlock", inv.selected.getUnlockLevel());
            holders.register("level", skillLevel);
            holders.register("roman", MMOCoreUtils.intToRoman(skillLevel));
            return holders;
        }

    }

    public class LevelItem extends PhysicalItem<SkillViewerInventory> {
        private final int offset;

        public LevelItem(ConfigurationSection config) {
            super(config);

            offset = config.getInt("offset");
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void preprocessLore(@NotNull SkillViewerInventory inv, int n, @NotNull List<String> lore) {
            ClassSkill skill = inv.selected;
            int skillLevel = inv.playerData.getSkillLevel(skill.getSkill()) + n - offset;
            if (skillLevel < 1) return;

            int index = lore.indexOf("{lore}");
            if (index >= 0) {
                lore.remove(index);
                lore.addAll(index, skill.calculateLore(inv.playerData, skillLevel));
            }
        }

        @Override
        public ItemStack getDisplayedItem(SkillViewerInventory inv, int n) {

            ClassSkill skill = inv.selected;
            int skillLevel = inv.playerData.getSkillLevel(skill.getSkill()) + n - offset;
            if (skillLevel < 1) return new ItemStack(Material.AIR);

            return super.getDisplayedItem(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            ClassSkill skill = inv.selected;
            int skillLevel = inv.playerData.getSkillLevel(skill.getSkill()) + n - offset;

            Placeholders holders = new Placeholders();
            holders.register("skill", inv.selected.getSkill().getName());
            holders.register("roman", MMOCoreUtils.intToRoman(skillLevel));
            holders.register("level", skillLevel);

            return holders;
        }
    }

    public class SlotItem extends PhysicalItem<SkillViewerInventory> {
        private final String none;
        @Nullable
        private final Material filledItem;
        private final int filledCMD;

        public SlotItem(ConfigurationSection config) {
            super(config);

            none = MythicLib.plugin.parseColors(config.getString("no-skill"));

            filledItem = config.contains("filled-item") ? Material.valueOf(config.getString("filled-item").toUpperCase().replace("-", "_").replace(" ", "_")) : null;
            filledCMD = config.getInt("filled-custom-model-data");
        }

        @Override
        public void preprocessLore(@NotNull SkillViewerInventory inv, int n, @NotNull List<String> lore) {

            // Slot lore
            final @Nullable SkillSlot skillSlot = inv.playerData.getProfess().getSkillSlot(n + 1);
            if (skillSlot == null || !inv.playerData.hasUnlocked(skillSlot)) return;

            int index = lore.indexOf("{slot-lore}");
            if (index != -1) {
                lore.remove(index);
                lore.addAll(index, skillSlot.getLore());
            }

            // Bound skill lore
            index = lore.indexOf("{skill-lore}");
            if (index != -1) {
                lore.remove(index);
                final @Nullable ClassSkill boundSkill = inv.playerData.getBoundSkill(n + 1);
                if (boundSkill != null) lore.addAll(index, boundSkill.calculateLore(inv.playerData));
            }
        }

        @Override
        public String preprocessName(@NotNull SkillViewerInventory inv, int n, @NotNull String name) {
            final @Nullable SkillSlot skillSlot = inv.playerData.getProfess().getSkillSlot(n + 1);
            if (skillSlot == null) return name;
            return skillSlot.getName();
        }

        @Override
        public ItemStack getDisplayedItem(SkillViewerInventory inv, int n) {
            final @Nullable SkillSlot skillSlot = inv.playerData.getProfess().getSkillSlot(n + 1);
            if (skillSlot == null || !inv.playerData.hasUnlocked(skillSlot)) return new ItemStack(Material.AIR);

            final @Nullable ClassSkill boundSkill = inv.playerData.getBoundSkill(n + 1);
            final ItemOptions options = boundSkill == null ? ItemOptions.index(n) : filledItem == null ? boundSkill.getSkill().getRawIcon().toItemOptions(n) : ItemOptions.model(n, filledItem, filledCMD);
            return super.getDisplayedItem(inv, options);
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            RegisteredSkill selected = inv.selected.getSkill();
            final @NotNull SkillSlot skillSlot = inv.playerData.getProfess().getSkillSlot(n + 1);
            Placeholders holders = new Placeholders();
            holders.register("slot", skillSlot.getName());
            holders.register("selected", selected == null ? none : selected.getName());
            RegisteredSkill skill = inv.playerData.hasSkillBound(n + 1) ? inv.playerData.getBoundSkill(n + 1).getSkill() : null;
            holders.register("skill", skill == null ? none : skill.getName());
            return holders;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void onClick(@NotNull SkillViewerInventory inv, @NotNull InventoryClickEvent event) {
            int index = inv.slotSlots.indexOf(event.getSlot()) + 1;
            SkillSlot skillSlot = inv.playerData.getProfess().getSkillSlot(index);

            // Select if the player is doing Shift Left Click
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                if (inv.playerData.hasSkillBound(index)) inv.selected = inv.playerData.getBoundSkill(index);
                return;
            }

            // unbind if there is a current spell.
            if (event.getClick() == ClickType.RIGHT) {
                if (!inv.playerData.hasSkillBound(index)) {
                    ConfigMessage.fromKey("no-skill-bound").send(inv.getPlayer());
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }
                if (!inv.playerData.getProfess().getSkillSlot(index).canManuallyBind()) {
                    ConfigMessage.fromKey("cant-manually-bind").send(inv.getPlayer());
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                inv.playerData.unbindSkill(index);
                inv.open();
                return;
            }

            if (inv.selected.isPermanent()) {
                ConfigMessage.fromKey("skill-cannot-be-bound").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (!inv.playerData.hasUnlockedLevel(inv.selected)) {
                ConfigMessage.fromKey("skill-level-not-met").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (!skillSlot.canManuallyBind()) {
                ConfigMessage.fromKey("cant-manually-bind").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (!skillSlot.acceptsSkill(inv.selected)) {
                ConfigMessage.fromKey("not-compatible-skill").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            inv.playerData.bindSkill(index, inv.selected);
            inv.open();
        }
    }

    //private static final NamespacedKey SKILL_ID_KEY = new NamespacedKey(MMOCore.plugin, "skill_id");

    public class SkillItem extends PhysicalItem<SkillViewerInventory> {
        public SkillItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void preprocessLore(@NotNull SkillViewerInventory inv, int n, @NotNull List<String> lore) {

            // Calculate placeholders
            int index = inv.getPageIndex(n);
            if (index >= inv.skills.size()) return;

            ClassSkill skill = inv.skills.get(index);
            boolean unlocked = skill.getUnlockLevel() <= inv.playerData.getLevel();

            var loreIdx = lore.indexOf("{lore}");
            if (loreIdx >= 0) {
                lore.remove(loreIdx);
                lore.addAll(loreIdx, skill.calculateLore(inv.playerData));
            }

            lore.removeIf(next -> (next.startsWith("{unlocked}") && !unlocked)
                    || (next.startsWith("{locked}") && unlocked)
                    || (next.startsWith("{max_level}") && (!skill.hasMaxLevel() || skill.getMaxLevel() > inv.playerData.getSkillLevel(skill.getSkill()))));

            for (int i = 0; i < lore.size(); i++) {
                String str = lore.get(i);
                if (str.startsWith("{unlocked}")) lore.set(i, str.substring("{unlocked}".length()));
                else if (str.startsWith("{locked}")) lore.set(i, str.substring("{locked}".length()));
                else if (str.startsWith("{max_level}")) lore.set(i, str.substring("{max_level}".length()));
            }
        }

        @Override
        public ItemStack getDisplayedItem(SkillViewerInventory inv, int n) {
            int index = inv.getPageIndex(n);
            if (index >= inv.skills.size()) return new ItemStack(Material.AIR);

            ClassSkill skill = inv.skills.get(index);
            return getDisplayedItem(inv, skill.getSkill().getRawIcon().toItemOptions(n));
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            int index = inv.getPageIndex(n);
            if (index >= inv.skills.size()) return new Placeholders();

            ClassSkill skill = inv.skills.get(index);

            Placeholders holders = new Placeholders();
            holders.register("skill", skill.getSkill().getName());
            holders.register("unlock", skill.getUnlockLevel());
            holders.register("level", inv.playerData.getSkillLevel(skill.getSkill()));
            return holders;
        }

        @Override
        public void onClick(@NotNull SkillViewerInventory inv, @NotNull InventoryClickEvent event) {
            var clickSlot = inv.skillSlots.indexOf(event.getSlot());
            var index = inv.getPageIndex(clickSlot);
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            inv.selected = inv.skills.get(index);
            inv.open();
        }
    }

    public class UpgradeItem extends PhysicalItem<SkillViewerInventory> {
        private int shiftCost = 1;

        public UpgradeItem(ConfigurationSection config) {
            super(config);

            if (config.contains("shift-cost")) {
                this.shiftCost = config.getInt("shift-cost");
                Validate.isTrue(shiftCost >= 1, "Upgrade shift-cost msut be 1 or above");
            }
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            RegisteredSkill selected = inv.selected == null ? null : inv.selected.getSkill();
            Placeholders holders = new Placeholders();

            holders.register("skill_caps", selected.getName().toUpperCase());
            holders.register("skill", selected.getName());
            holders.register("skill_points", inv.playerData.getSkillPoints());
            holders.register("shift_points", shiftCost);
            return holders;
        }

        @Override
        public void onClick(@NotNull SkillViewerInventory inv, @NotNull InventoryClickEvent event) {

            if (!inv.playerData.hasUnlockedLevel(inv.selected)) {
                ConfigMessage.fromKey("skill-level-not-met").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (!inv.selected.isUpgradable()) {
                ConfigMessage.fromKey("cannot-upgrade-skill").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (inv.playerData.getSkillPoints() < 1) {
                ConfigMessage.fromKey("not-enough-skill-points").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (inv.selected.hasMaxLevel() && inv.playerData.getSkillLevel(inv.selected.getSkill()) >= inv.selected.getMaxLevel()) {
                ConfigMessage.fromKey("skill-max-level-hit").send(inv.getPlayer());
                inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            if (event.getClick().isShiftClick()) {
                if (inv.playerData.getSkillPoints() < shiftCost) {
                    ConfigMessage.fromKey("not-enough-skill-points-shift", "shift_points", "" + shiftCost).send(inv.getPlayer());
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                inv.playerData.giveSkillPoints(-shiftCost);
                inv.playerData.setSkillLevel(inv.selected.getSkill(), inv.playerData.getSkillLevel(inv.selected.getSkill()) + shiftCost);
            } else {
                inv.playerData.giveSkillPoints(-1);
                inv.playerData.setSkillLevel(inv.selected.getSkill(), inv.playerData.getSkillLevel(inv.selected.getSkill()) + 1);
            }

            ConfigMessage.fromKey("upgrade-skill", "skill", inv.selected.getSkill().getName(), "level", inv.playerData.getSkillLevel(inv.selected.getSkill())).send(inv.getPlayer());
            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
            inv.open();
        }
    }

    public class SkillViewerInventory extends GeneratedInventory {

        // Cached information
        private final List<ClassSkill> skills;
        private final List<Integer> skillSlots;
        private final List<Integer> slotSlots;

        // Skill the player selected
        private ClassSkill selected;

        private final PlayerData playerData;

        public SkillViewerInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), SkillList.this);

            this.playerData = playerData;
            skills = playerData.getProfess().getSkills().stream().filter(playerData::hasUnlocked).sorted(Comparator.comparingInt(ClassSkill::getUnlockLevel)).collect(Collectors.toList());
            skillSlots = getEditable().getByFunction("skill").getSlots();

            Validate.notNull(getEditable().getByFunction("slot"), "Your skill GUI config file is out-of-date, please regenerate it.");
            slotSlots = getEditable().getByFunction("slot").getSlots();
            selected = skills.get(0);

            enablePagination(skillSlots.size());
        }

        @Override
        public @NotNull String getRawName() {
            return guiName.replace("{skill}", selected.getSkill().getName());
        }

        @Override
        public int getMaxPage() {
            return computeMaxPage(skills.size());
        }
    }
}