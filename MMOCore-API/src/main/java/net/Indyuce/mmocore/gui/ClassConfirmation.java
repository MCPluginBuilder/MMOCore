package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.GoBackItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.player.ClassDataContainer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassConfirmation extends AbstractClassSelect {
    private final PlayerClass playerClass;

    public ClassConfirmation(PlayerClass playerClass, boolean isDefault) {
        super("class-confirm-" + (isDefault ? "default" : UtilityMethods.ymlName(playerClass.getId())));

        this.playerClass = playerClass;
    }

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("yes")) return new YesItem(config);
        if (function.equalsIgnoreCase("back")) return new GoBackItem<>(config);
        return null;
    }

    public GeneratedInventory newInventory(Navigator navigator, PlayerData playerData, boolean subclass) {
        return newInventory(navigator, playerData, subclass, null);
    }

    public GeneratedInventory newInventory(Navigator navigator, PlayerData playerData, boolean subclass, @Nullable Runnable profileRunnable) {
        return new ClassConfirmationInventory(navigator, playerData, playerClass, subclass, profileRunnable);
    }

    public class UnlockedItem extends PhysicalItem<ClassConfirmationInventory> {
        public UnlockedItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
            PlayerClass profess = inv.profess;
            ClassDataContainer info = inv.subclass ? inv.playerData : inv.playerData.getClassInfo(profess);
            Placeholders holders = new Placeholders();

            final double nextLevelExp = inv.playerData.getLevelUpExperience();
            final double ratio = info.getExperience() / nextLevelExp;

            StringBuilder bar = new StringBuilder("" + ChatColor.BOLD);
            int chars = (int) (ratio * 20);
            for (int j = 0; j < 20; j++)
                bar.append(j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "").append("|");

            holders.register("percent", ONE_DIGIT.format(ratio * 100));
            holders.register("progress", bar.toString());
            holders.register("class", profess.getName());
            holders.register("unlocked_skills", info.mapSkillLevels().size());
            holders.register("class_skills", profess.getSkills().size());
            holders.register("next_level", "" + nextLevelExp);
            holders.register("level", info.getLevel());
            holders.register("exp", info.getExperience());
            holders.register("skill_points", info.getSkillPoints());

            return holders;
        }
    }

    public class YesItem extends InventoryItem<ClassConfirmationInventory> {
        private final InventoryItem<ClassConfirmationInventory> unlocked, locked;

        public YesItem(ConfigurationSection config) {
            super(config);

            Validate.isTrue(config.contains("unlocked"), "Could not load 'unlocked' config");
            Validate.isTrue(config.contains("locked"), "Could not load 'locked' config");

            unlocked = new UnlockedItem(config.getConfigurationSection("unlocked"));
            locked = new PhysicalItem<>(config.getConfigurationSection("locked")) {

                @Override
                public Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    holders.register("class", inv.profess.getName());
                    return holders;
                }
            };
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull ClassConfirmationInventory inv, int n) {
            return inv.playerData.hasSavedClass(inv.profess) ? unlocked.getDisplayedItem(inv, n) : locked.getDisplayedItem(inv, n);
        }

        @Override
        public void onClick(@NotNull ClassConfirmationInventory inv, @NotNull InventoryClickEvent event) {
            PlayerChangeClassEvent called = new PlayerChangeClassEvent(inv.playerData, inv.profess);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return;

            inv.getNavigator().unblockClosing();
            inv.playerData.giveClassPoints(-1);
            if (inv.subclass) inv.playerData.setClass(inv.profess);
            else
                (inv.playerData.hasSavedClass(inv.profess) ? inv.playerData.getClassInfo(inv.profess)
                        : new SavedClassInformation(MMOCore.plugin.playerDataManager.getDefaultData())).load(inv.profess, inv.playerData);
            ConfigMessage.fromKey("class-select", "class", inv.profess.getName()).send(inv.playerData);
            MMOCore.plugin.soundManager.getSound(SoundEvent.SELECT_CLASS).playTo(inv.getPlayer());
            inv.getPlayer().closeInventory();
        }
    }

    public class ClassConfirmationInventory extends AbstractClassGeneratedInventory {
        private final PlayerClass profess;
        private final boolean subclass;

        public ClassConfirmationInventory(Navigator navigator, PlayerData playerData, PlayerClass profess, boolean subclass, @Nullable Runnable profileRunnable) {
            super(navigator, playerData, profileRunnable);

            this.profess = profess;
            this.subclass = subclass;
        }

        @NotNull
        @Override
        public String getRawName() {
            return guiName.replace("{class}", profess.getName());
        }
    }
}
