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
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Profession;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestViewer extends EditableInventory {
    public QuestViewer() {
        super("quest-list");
    }

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        if (function.equals("quest")) return new QuestItem(config);

        if (function.equals("previous")) return new PreviousPageItem<>(config);
        if (function.equals("next")) return new NextPageItem<>(config);

        return null;
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new QuestInventory(data, this);
    }

    public class QuestItem extends PhysicalItem<QuestInventory> {
        private final SimpleItem<QuestInventory> noQuest, locked;

        private final String mainHit, mainNotHit, professionHit, professionNotHit;
        private final SimpleDateFormat dateFormat;

        public QuestItem(ConfigurationSection config) {
            super(config);

            Validate.isTrue(config.contains("no-quest"), "Could not load config 'no-quest'");
            Validate.isTrue(config.contains("locked"), "Could not load config 'locked'");

            locked = new SimpleItem<>(config.getConfigurationSection("locked"));
            noQuest = new SimpleItem<>(config.getConfigurationSection("no-quest"));

            Validate.isTrue(config.contains("date-format"), "Could not find date-format");
            dateFormat = new SimpleDateFormat(config.getString("date-format"));

            Validate.notNull(mainHit = config.getString("level-requirement.main.hit"), "Could not load 'level-requirement.main.hit'");
            Validate.notNull(mainNotHit = config.getString("level-requirement.main.not-hit"), "Could not load 'level-requirement.main.not-hit'");
            Validate.notNull(professionHit = config.getString("level-requirement.profession.hit"),
                    "Could not load 'level-requirement.profession.hit'");
            Validate.notNull(professionNotHit = config.getString("level-requirement.profession.not-hit"),
                    "Could not load 'level-requirement.profession.not-hit'");
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(QuestInventory inv, int itemIndex) {
            final int index = inv.page * inv.perPage + itemIndex;

            if (index >= inv.quests.size())
                return noQuest.getDisplayedItem(inv, itemIndex);

            Quest quest = inv.quests.get(index);
            if (quest.hasParent() && !inv.playerData.getQuestData().checkParentAvailability(quest))
                return locked.getDisplayedItem(inv, itemIndex);

            ItemStack item = super.getDisplayedItem(inv, itemIndex);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>(meta.getLore());

            // Replace quest lore
            int loreIndex = lore.indexOf(ChatColor.GRAY + "{lore}");
            if (loreIndex >= 0) {
                lore.remove(loreIndex);
                for (int j = 0; j < quest.getLore().size(); j++)
                    lore.add(loreIndex + j, quest.getLore().get(j));
            }

            // Calculate lore for later
            int reqCount = quest.countLevelRestrictions();
            boolean started = inv.playerData.getQuestData().hasCurrent(quest), completed = inv.playerData.getQuestData().hasFinished(quest),
                    cooldown = completed && inv.playerData.getQuestData().checkCooldownAvailability(quest);

            lore.removeIf(next -> (next.startsWith(ChatColor.GRAY + "{level_req}") && reqCount < 1)
                    || (next.startsWith(ChatColor.GRAY + "{started}") && !started)
                    || (next.startsWith(ChatColor.GRAY + "{!started}") && started)
                    || (next.startsWith(ChatColor.GRAY + "{completed}") && !completed)
                    || (next.startsWith(ChatColor.GRAY + "{completed_cannot_redo}") && !(completed && !quest.isRedoable()))
                    || (next.startsWith(ChatColor.GRAY + "{completed_can_redo}") && !(cooldown && quest.isRedoable()))
                    || (next.startsWith(ChatColor.GRAY + "{completed_delay}") && !(completed && !cooldown)));

            // Replace level requirements
            loreIndex = lore.indexOf(ChatColor.GRAY + "{level_req}{level_requirements}");
            if (loreIndex >= 0) {
                lore.remove(loreIndex);
                int mainRequired = quest.getLevelRestriction(null);
                if (mainRequired > 0)
                    lore.add(loreIndex, (inv.playerData.getLevel() >= mainRequired ? mainHit : mainNotHit).replace("{level}", "" + mainRequired));

                for (Profession profession : quest.getLevelRestrictions()) {
                    int required = quest.getLevelRestriction(profession);
                    lore.add(loreIndex + (mainRequired > 0 ? 1 : 0),
                            (inv.playerData.getCollectionSkills().getLevel(profession) >= required ? professionHit : professionNotHit)
                                    .replace("{level}", "" + required).replace("{profession}", profession.getName()));
                }
            }

            Placeholders holders = getPlaceholders(inv, itemIndex);
            lore.replaceAll(str -> ChatColor.GRAY + holders.apply(inv.getPlayer(), str));

            // Generate item
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(MMOCore.plugin, "quest_id"), PersistentDataType.STRING, quest.getId());
            item.setItemMeta(meta);

            return item;
        }

        @Override
        public Placeholders getPlaceholders(QuestInventory inv, int itemIndex) {
            final Quest quest = inv.quests.get(inv.page * inv.perPage + itemIndex);
            PlayerData data = inv.playerData;

            Placeholders holders = new Placeholders();
            holders.register("name", quest.getName());
            holders.register("total_level_req", quest.getLevelRestrictions().size() + (quest.getLevelRestriction(null) > 0 ? 1 : 0));
            holders.register("current_level_req", (data.getLevel() >= quest.getLevelRestriction(null) ? 1 : 0) + quest.getLevelRestrictions().stream()
                    .filter(type -> data.getCollectionSkills().getLevel(type) >= quest.getLevelRestriction(type)).collect(Collectors.toSet()).size());

            if (data.getQuestData().hasCurrent(quest)) {
                holders.register("objective", data.getQuestData().getCurrent().getFormattedLore());
                holders.register("progress",
                        (int) ((double) data.getQuestData().getCurrent().getObjectiveNumber() / quest.getObjectives().size() * 100.));
            }

            if (data.getQuestData().hasFinished(quest)) {
                holders.register("date", dateFormat.format(data.getQuestData().getFinishDate(quest)));
                holders.register("delay", new DelayFormat(2).format(data.getQuestData().getDelayFeft(quest)));
            }

            return holders;
        }

        @Override
        public void onClick(@NotNull QuestInventory inv, @NotNull InventoryClickEvent event) {
            String questId = event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(MMOCore.plugin, "quest_id"), PersistentDataType.STRING);
            if (questId == null || questId.equals(""))
                return;

            Quest quest = MMOCore.plugin.questManager.get(questId);

            if (inv.playerData.getQuestData().hasCurrent()) {

                // Check if the player is cancelling his ongoing quest
                if (inv.playerData.getQuestData().hasCurrent(quest)) {
                    if (event.getClick() == ClickType.RIGHT) {
                        inv.playerData.getQuestData().start(null);
                        MMOCore.plugin.soundManager.getSound(SoundEvent.CANCEL_QUEST).playTo(inv.getPlayer());
                        ConfigMessage.fromKey("cancel-quest").send(inv.playerData);
                        inv.open();
                    }
                    return;
                }

                // The player cannot start a new quest if he is already doing one
                ConfigMessage.fromKey("already-on-quest").send(inv.playerData);
                return;
            }

            // Check for level requirements.
            int level;
            if (inv.playerData.getLevel() < (level = quest.getLevelRestriction(null))) {
                ConfigMessage.fromKey("quest-level-restriction", "level", "Lvl", "count", "" + level).send(inv.playerData);
                return;
            }

            for (Profession profession : quest.getLevelRestrictions())
                if (inv.playerData.getCollectionSkills().getLevel(profession) < (level = quest.getLevelRestriction(profession))) {
                    ConfigMessage.fromKey("quest-level-restriction", "level", profession.getName() + " Lvl", "count", "" + level)
                            .send(inv.playerData);
                    return;
                }

            if (inv.playerData.getQuestData().hasFinished(quest)) {

                // If the player has already finished this quest, he can't start it again
                if (!quest.isRedoable()) {
                    ConfigMessage.fromKey("cant-redo-quest").send(inv.playerData);
                    return;
                }

                // Has the player waited long enough
                if (!inv.playerData.getQuestData().checkCooldownAvailability(quest)) {
                    ConfigMessage.fromKey("quest-cooldown", "delay", new DelayFormat(2).format(inv.playerData.getQuestData().getDelayFeft(quest))).send(inv.playerData);
                    return;
                }
            }

            // Eventually start the quest
            ConfigMessage.fromKey("start-quest", "quest", quest.getName()).send(inv.playerData);
            MMOCore.plugin.soundManager.getSound(SoundEvent.START_QUEST).playTo(inv.getPlayer());
            inv.playerData.getQuestData().start(quest);
            inv.open();
        }
    }

    public class QuestInventory extends GeneratedInventory {
        private final List<Quest> quests = new ArrayList<>(MMOCore.plugin.questManager.getAll());
        private final int perPage;
        private final PlayerData playerData;

        public QuestInventory(PlayerData playerData, EditableInventory editable) {
            super(new Navigator(playerData.getMMOPlayerData()), editable);

            this.playerData = playerData;
            perPage = editable.getByFunction("quest").getSlots().size();
        }
    }
}
