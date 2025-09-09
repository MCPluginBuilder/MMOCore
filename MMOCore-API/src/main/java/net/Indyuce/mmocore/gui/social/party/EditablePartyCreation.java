package net.Indyuce.mmocore.gui.social.party;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.CloseInventoryItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditablePartyCreation extends EditableInventory {
    public EditablePartyCreation() {
        super("party-creation");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("create")) return new CreateItem(config);
        if (function.equalsIgnoreCase("back")) return new CloseInventoryItem<>(config);
        return null;
    }

    public ClassConfirmationInventory newInventory(PlayerData data) {
        return new ClassConfirmationInventory(data);
    }

    public class CreateItem extends SimpleItem<ClassConfirmationInventory> {
        public CreateItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull ClassConfirmationInventory inv, @NotNull InventoryClickEvent event) {
            ((MMOCorePartyModule) MMOCore.plugin.partyModule).newRegisteredParty(inv.playerData);
            InventoryManager.PARTY_VIEW.newInventory(inv.playerData).open();
            Message.PARTY_CREATED.send(inv.getPlayer());
        }
    }

    public class ClassConfirmationInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public ClassConfirmationInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditablePartyCreation.this);

            this.playerData = playerData;
        }
    }
}
