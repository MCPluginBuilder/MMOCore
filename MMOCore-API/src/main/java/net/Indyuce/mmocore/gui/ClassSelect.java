package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassSelect extends AbstractClassSelect {
    public ClassSelect() {
        super("class-select");
    }

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.startsWith("class")) return new ClassItem(config);
        return null;
    }

    public ProfessSelectionInventory newInventory(PlayerData data) {
        return newInventory(data, null);
    }

    public ProfessSelectionInventory newInventory(PlayerData data, @Nullable Runnable profileRunnable) {
        return new ProfessSelectionInventory(data, profileRunnable);
    }

    public class ClassItem extends AbstractClassItem<ProfessSelectionInventory> {
        public ClassItem(ConfigurationSection config) {
            super(config, "class-".length());
        }

        @Override
        public void onClick(@NotNull ProfessSelectionInventory inv, @NotNull InventoryClickEvent event) {

            if (inv.profileCallback == null && inv.playerData.getClassPoints() < 1) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("cant-choose-new-class").send(inv.playerData);
                return;
            }

            if (playerClass.hasOption(ClassOption.NEEDS_PERMISSION) && !inv.getPlayer().hasPermission("mmocore.class." + playerClass.getId().toLowerCase())) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("no-permission-for-class").send(inv.playerData);
                return;
            }

            if (playerClass.equals(inv.playerData.getProfess())) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(inv.getPlayer());
                ConfigMessage.fromKey("already-on-class", "class", playerClass.getName()).send(inv.getPlayer());
                return;
            }

            inv.getNavigator().unblockClosing();
            final PlayerClass playerClass = findDeepestSubclass(inv.playerData, this.playerClass);
            InventoryManager.CLASS_CONFIRM.get(UtilityMethods.kebabCase(playerClass.getId())).newInventory(inv, inv.profileCallback != null).open();
        }
    }

    public class ProfessSelectionInventory extends AbstractClassGeneratedInventory {
        public ProfessSelectionInventory(PlayerData playerData, @Nullable Runnable profileRunnable) {
            super(new Navigator(playerData.getMMOPlayerData()), playerData, profileRunnable);
        }
    }

    /**
     * When switching from a class where you had progress before,
     * you should be instantly redirected to the highest subclass
     * in the subclass tree that you chose, because your progress
     * is saved there.
     * <p>
     * It's also more RPG style to take the player back to the subclass
     * he chose because that way he can't turn back and chose another path.
     * <p>
     * This does NOT function properly with subclass nets yet.
     *
     * @param root The root class, it's called the root because since the
     *             player was able to choose it in the class GUI, it should
     *             be at the bottom of the class tree.
     */
    private PlayerClass findDeepestSubclass(PlayerData player, PlayerClass root) {
        for (String checkedName : player.getSavedClasses()) {
            PlayerClass checked = MMOCore.plugin.classManager.getOrThrow(checkedName);
            if (root.hasSubclass(checked))
                return checked;
        }

        return root;
    }
}
