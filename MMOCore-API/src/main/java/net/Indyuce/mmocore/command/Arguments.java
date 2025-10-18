package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.command.argument.ArgumentParseException;
import io.lumine.mythic.lib.command.argument.PermissionException;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.experience.Booster;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.Indyuce.mmocore.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Arguments {

    public static final Argument<@NotNull PlayerClass> CLASS = new Argument<>("class",
            (explorer, list) -> MMOCore.plugin.classManager.getAll().forEach(profess -> list.add(profess.getId())),
            (explorer, input) -> {
                final var profess = MMOCore.plugin.classManager.get(input.toUpperCase().replace("-", "_"));
                if (profess == null) throw new ArgumentParseException("Could not find class with ID '" + input + "'");
                return profess;
            });

    public static final Argument<@NotNull Waypoint> WAYPOINT = new Argument<>("waypoint",
            (explorer, list) -> MMOCore.plugin.waypointManager.getAll().forEach(way -> list.add(way.getId())),
            (explorer, input) -> {
                final var waypoint = MMOCore.plugin.waypointManager.get(input);
                if (waypoint == null)
                    throw new ArgumentParseException("Could not find waypoint with ID '" + input + "'");
                return waypoint;
            });

    public static final Argument<@Nullable String> ACCEPT_OR_DENY_OPTIONAL = Argument.choices("accept/deny", "accept", "deny").withFallback(explorer -> null);

    public static final Argument<@NotNull Player> PLAYER_IF_OP = new Argument<>("player",
            (explorer, list) -> {
                if (!explorer.getSender().hasPermission("mmocore.admin")) return;
                Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName()));
            },
            (explorer, input) -> {
                if (!explorer.getSender().hasPermission("mmocore.admin")) throw new PermissionException();

                final var player = Bukkit.getPlayer(input);
                Validate.notNull(player, "Could not find player " + input);
                return player;
            }, explorer -> {
        if (explorer.getSender() instanceof Player) return (Player) explorer.getSender();
        throw new ArgumentParseException("Please provide a player");
    });

    public static final Argument<@NotNull PlayerAttribute> ATTRIBUTE = new Argument<>("attribute",
            (explorer, list) -> MMOCore.plugin.attributeManager.getAll().forEach(attribute -> list.add(attribute.getId())),
            (explorer, input) -> {
                final var attribute = MMOCore.plugin.attributeManager.get(input);
                if (attribute == null)
                    throw new ArgumentParseException("Could not find attribute with ID '" + input + "'");
                return attribute;
            });

    public static final Argument<@NotNull RegisteredSkill> SKILL = new Argument<>("skill",
            (explorer, list) -> MMOCore.plugin.skillManager.getAll().forEach(skill -> list.add(skill.getHandler().getId().toUpperCase())),
            (explorer, input) -> {
                final var skill = MMOCore.plugin.skillManager.getSkill(input);
                if (skill == null) throw new ArgumentParseException("Could not find skill with ID '" + input + "'");
                return skill;
            });

    public static final Argument<Profession> PROFESSION = new Argument<>("profession/main", (explorer, list) -> {
        MMOCore.plugin.professionManager.getAll().forEach(profession -> list.add(profession.getId()));
        list.add("main");
    }, (explorer, input) -> {
        if (input.equalsIgnoreCase("main")) return null;
        final var profession = MMOCore.plugin.professionManager.get(input);
        if (profession == null) throw new ArgumentParseException("Could not find profession with ID '" + input + "'");
        return profession;
    });

    public static final Argument<Booster> BOOSTER = new Argument<>("booster_id",
            (explorer, list) -> MMOCore.plugin.boosterManager.getActive().forEach(booster -> list.add(String.valueOf(booster.getUniqueId()))),
            (explorer, input) -> {
                try {
                    final var uuid = java.util.UUID.fromString(input);
                    for (Booster booster : MMOCore.plugin.boosterManager.getActive())
                        if (booster.getUniqueId().equals(uuid))
                            return booster;
                    throw new ArgumentParseException("Could not find active booster with ID '" + input + "'");
                } catch (IllegalArgumentException exception) {
                    throw new ArgumentParseException("Invalid UUID '" + input + "'");
                }
            });

    public static final Argument<Quest> QUEST = new Argument<>("quest",
            (explorer, list) -> MMOCore.plugin.questManager.getAll().forEach(quest -> list.add(quest.getId())),
            (explorer, input) -> {
                final var quest = MMOCore.plugin.questManager.get(input);
                if (quest == null) throw new ArgumentParseException("Could not find quest with ID '" + input + "'");
                return quest;
            });

    public static final Argument<SkillTree> SKILL_TREE = new Argument<>("skill_tree_id",
            (explorer, list) -> MMOCore.plugin.skillTreeManager.getAll().forEach(skillTree -> list.add(skillTree.getId())),
            (explorer, input) -> {
                final var skillTree = MMOCore.plugin.skillTreeManager.get(input);
                if (skillTree == null)
                    throw new ArgumentParseException("Could not find skill tree with ID '" + input + "'");
                return skillTree;
            });

    public static final String SKILL_TREE_GLOBAL_KEY = "global";

    public static final Argument<@Nullable SkillTree> SKILL_TREE_OR_GLOBAL = new Argument<>("skill_tree_id",
            (explorer, list) -> {
                list.add("global");
                MMOCore.plugin.skillTreeManager.getAll().forEach(skillTree -> list.add(skillTree.getId()));
            },
            (explorer, input) -> {
                if (input.equalsIgnoreCase(SKILL_TREE_GLOBAL_KEY)) return null;
                final var skillTree = MMOCore.plugin.skillTreeManager.get(input);
                if (skillTree == null)
                    throw new ArgumentParseException("Could not find skill tree with ID '" + input + "' and not '" + SKILL_TREE_GLOBAL_KEY + "'");
                return skillTree;
            });

    public static final Argument<@NotNull Integer> INDEX = Argument.AMOUNT_INT
            .withKey("index")
            .withAutoComplete((explorer, list) -> {
                for (int j = 1; j <= 9; j++)
                    list.add(String.valueOf(j));
            });
}
