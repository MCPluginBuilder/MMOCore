package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.CloseInventoryItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditableGuildCreation extends EditableInventory {
    public EditableGuildCreation() {
        super("guild-creation");
    }

    @Override
    public @Nullable InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equalsIgnoreCase("create")) return new CreateItem(config);
        if (function.equalsIgnoreCase("back")) return new CloseInventoryItem<>(config);

        return null;
    }

    public class CreateItem extends SimpleItem<GuildCreationInventory> {
        public CreateItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull GuildCreationInventory inv, @NotNull InventoryClickEvent event) {
            new ChatInput(inv.getPlayer(), PlayerInput.InputType.GUILD_CREATION_TAG, inv, input -> {
                if (MMOCore.plugin.nativeGuildManager.getConfig().shouldUppercaseTags())
                    input = input.toUpperCase();

                if (check(inv.getPlayer(), input, MMOCore.plugin.nativeGuildManager.getConfig().getTagRules())) {
                    String tag = input;

                    new ChatInput(inv.getPlayer(), PlayerInput.InputType.GUILD_CREATION_NAME, inv, name -> {
                        if (check(inv.getPlayer(), name, MMOCore.plugin.nativeGuildManager.getConfig().getNameRules())) {
                            MMOCore.plugin.nativeGuildManager.newRegisteredGuild(inv.playerData.getUniqueId(), name, tag);
                            MMOCore.plugin.nativeGuildManager.getGuild(tag.toLowerCase()).addMember(inv.playerData.getUniqueId());

                            InventoryManager.GUILD_VIEW.newInventory(inv.playerData).open();
                            inv.getPlayer().playSound(inv.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    });
                }
            });
        }
    }

    public GuildCreationInventory newInventory(PlayerData data) {
        return new GuildCreationInventory(data);
    }

    public class GuildCreationInventory extends GeneratedInventory {
        private final PlayerData playerData;

        public GuildCreationInventory(PlayerData playerData) {
            super(new Navigator(playerData.getMMOPlayerData()), EditableGuildCreation.this);

            this.playerData = playerData;
        }
    }

    public boolean check(Player player, String input, GuildDataManager.GuildConfiguration.NamingRules rules) {
        String reason;

        if (input.length() <= rules.getMax() && input.length() >= rules.getMin())
            if (input.matches(rules.getRegex()))
                if (!MMOCore.plugin.nativeGuildManager.isRegistered(input))
                    return true;
                else
                    reason = ConfigMessage.fromKey("guild-creation.reasons.already-taken").asLine();
            else
                reason = ConfigMessage.fromKey("guild-creation.reasons.invalid-characters").asLine();
        else
            reason = ConfigMessage.fromKey("guild-creation.reasons.invalid-length", "min", "" + rules.getMin(), "max", "" + rules.getMax()).asLine();

        ConfigMessage.fromKey("guild-creation.failed", "reason", reason).send(player);
        return false;
    }
}
