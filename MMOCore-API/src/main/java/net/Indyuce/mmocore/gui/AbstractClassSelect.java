package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractClassSelect extends EditableInventory {
    public AbstractClassSelect(String id) {
        super(id);
    }

    public abstract class AbstractClassItem<T extends GeneratedInventory> extends SimpleItem<T> {
        protected final PlayerClass playerClass;

        public AbstractClassItem(ConfigurationSection config, int substringIndex) {
            super(config);

            Validate.isTrue(config.getString("function").length() > substringIndex, "Couldn't find the class associated to: " + config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(substringIndex));
            this.playerClass = MMOCore.plugin.classManager.getOrThrow(classId);
        }

        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public void preprocessLore(@NotNull T inv, int n, @NotNull List<String> lore) {

            int index = lore.indexOf("{lore}");
            if (index >= 0) {
                lore.remove(index);
                lore.addAll(index, playerClass.getDescription());
            }

            index = lore.indexOf("{attribute-lore}");
            if (index >= 0) {
                lore.remove(index);
                lore.addAll(index, playerClass.getAttributeDescription());
            }
        }

        @Override
        public ItemStack getDisplayedItem(T inv, int n) {
            ItemOptions options = n == 0 ? ItemOptions.item(n, playerClass.getIcon()) : ItemOptions.index(n);
            return super.getDisplayedItem(inv, options);
        }

        @Override
        public @NotNull Placeholders getPlaceholders(T inv, int n) {
            Placeholders placeholders = super.getPlaceholders(inv, n);
            placeholders.register("name", playerClass.getName());
            return placeholders;
        }
    }

    public abstract class AbstractClassGeneratedInventory extends GeneratedInventory {
        @Nullable
        public final Runnable profileRunnable;
        public final PlayerData playerData;

        public AbstractClassGeneratedInventory(Navigator navigator, PlayerData playerData, @Nullable Runnable profileRunnable) {
            super(navigator, AbstractClassSelect.this);

            this.playerData = playerData;
            this.profileRunnable = profileRunnable;
        }

        @Override
        public void open() {
            if (profileRunnable != null) getNavigator().blockClosing();
            super.open();
        }
    }
}
