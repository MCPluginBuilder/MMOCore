package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.gui.*;
import net.Indyuce.mmocore.gui.skilltree.SkillTreeViewer;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendList;
import net.Indyuce.mmocore.gui.social.friend.EditableFriendRemoval;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildCreation;
import net.Indyuce.mmocore.gui.social.guild.EditableGuildView;
import net.Indyuce.mmocore.gui.social.party.EditablePartyCreation;
import net.Indyuce.mmocore.gui.social.party.EditablePartyView;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InventoryManager {

    // GUIs
    public static final PlayerStats PLAYER_STATS = new PlayerStats();
    public static final SkillList SKILL_LIST = new SkillList();
    public static final ClassSelect CLASS_SELECT = new ClassSelect();
    public static final SubclassSelect SUBCLASS_SELECT = new SubclassSelect();
    public static final WaypointViewer WAYPOINTS = new WaypointViewer();
    public static final EditableFriendList FRIEND_LIST = new EditableFriendList();
    public static final EditableFriendRemoval FRIEND_REMOVAL = new EditableFriendRemoval();
    public static final EditablePartyView PARTY_VIEW = new EditablePartyView();
    public static final EditablePartyCreation PARTY_CREATION = new EditablePartyCreation();
    public static final EditableGuildView GUILD_VIEW = new EditableGuildView();
    public static final EditableGuildCreation GUILD_CREATION = new EditableGuildCreation();
    public static final QuestViewer QUEST_LIST = new QuestViewer();
    public static final AttributeView ATTRIBUTE_VIEW = new AttributeView();
    public static final SkillTreeViewer TREE_VIEW = new SkillTreeViewer();

    // Specific GUIs
    public static final Map<String, SkillTreeViewer> SPECIFIC_TREE_VIEW = new HashMap<>();
    public static final Map<String, ClassConfirmation> CLASS_CONFIRM = new HashMap<>();

    public static final List<EditableInventory> LIST = Arrays.asList(PLAYER_STATS, ATTRIBUTE_VIEW, TREE_VIEW, SKILL_LIST, CLASS_SELECT, SUBCLASS_SELECT, QUEST_LIST, WAYPOINTS, FRIEND_LIST, FRIEND_REMOVAL, PARTY_VIEW, PARTY_CREATION, GUILD_VIEW, GUILD_CREATION);
    @Deprecated
    public static final List<EditableInventory> list = LIST;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void load() {

        // Loads specific inventories
        for (var invType : InventoryDuplicate.values()) {

            // Copy default config
            try {
                MMOCore.plugin.configManager.copyDefaultFile("gui/" + invType.name + "/" + invType.name + "-default.yml");
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory 'gui/" + invType.name + "/" + invType.name + "-default" + "': " + exception.getMessage());
            }

            MMOCore.plugin.getLogger().log(Level.INFO, "For inv type " + invType.name+" got " + invType.ids.get());

            for (String id : invType.ids.get()) {
                final var formattedId = UtilityMethods.ymlName(id);
                final var configFile = new ConfigFile("/gui/" + invType.name, invType.name + "-" + formattedId);
                final var specificUi = invType.provider.apply(id, !configFile.exists());

                ((Map) invType.inventories).put(formattedId, specificUi);
                specificUi.reload(MMOCore.plugin, new ConfigFile("/gui/" + invType.name, specificUi.getId()).getConfig());
            }
        }

        LIST.forEach(inv -> {
            try {
                MMOCore.plugin.configManager.copyDefaultFile("gui/" + inv.getId() + ".yml");
                inv.reload(MMOCore.plugin, new ConfigFile("/gui", inv.getId()).getConfig());
            } catch (Exception exception) {
                MMOCore.log(Level.WARNING, "Could not load inventory '" + (inv instanceof ClassConfirmation ? "class-confirm/" : "") + inv.getId() + "': " + exception.getMessage());
            }
        });
    }

    private static enum InventoryDuplicate {
        CLASS_CONFIRM("class-confirm", InventoryManager.CLASS_CONFIRM,
                (id, isDefault) -> new ClassConfirmation(MMOCore.plugin.classManager.get(id), isDefault),
                () -> MMOCore.plugin.classManager.getAll().
                        stream().
                        map(PlayerClass::getId).
                        collect(Collectors.toList())),

        SPECIFIC_TREE("specific-skill-tree", InventoryManager.SPECIFIC_TREE_VIEW,
                (id, isDefault) -> new SkillTreeViewer(MMOCore.plugin.skillTreeManager.get(id), isDefault),
                () -> MMOCore.plugin.skillTreeManager.getAll().
                        stream().
                        map(SkillTree::getId).
                        collect(Collectors.toList()));

        private final String name;
        private final Map<String, ? extends EditableInventory> inventories;
        private final Supplier<List<String>> ids;
        private final BiFunction<String, Boolean, ? extends EditableInventory> provider;

        InventoryDuplicate(String name,
                           Map<String, ? extends EditableInventory> inventories,
                           BiFunction<String, Boolean, ? extends EditableInventory> provider,
                           Supplier<List<String>> ids) {
            this.name = name;
            this.inventories = inventories;
            this.ids = ids;
            this.provider = provider;
        }
    }
}
