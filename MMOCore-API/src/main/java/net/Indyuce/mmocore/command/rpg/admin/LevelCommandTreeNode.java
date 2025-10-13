package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class LevelCommandTreeNode extends CommandTreeNode {
    public LevelCommandTreeNode(CommandTreeNode parent) {
        super(parent, "level");

        addChild(new ActionCommandTreeNode(this, "set",
                (player, newLevel) -> player.setLevel(newLevel, PlayerLevelChangeEvent.Reason.COMMAND),
                (player, profession, amount) -> player.setLevel(profession, amount, PlayerLevelChangeEvent.Reason.COMMAND)));
        addChild(new ActionCommandTreeNode(this, "give",
                (data, value) -> data.giveLevels(value, EXPSource.COMMAND),
                (professions, profession, value) -> professions.giveLevels(profession, value, EXPSource.COMMAND)));
        addChild(new ActionCommandTreeNode(this, "take",
                (playerData, amount) -> playerData.setLevel(playerData.getLevel() - amount, PlayerLevelChangeEvent.Reason.COMMAND),
                (player, profession, amount) -> player.setLevel(profession, player.getLevel(profession) - amount, PlayerLevelChangeEvent.Reason.COMMAND)));
    }

    public static class ActionCommandTreeNode extends CommandTreeNode {
        private final BiConsumer<PlayerData, Integer> main;
        private final TriConsumer<PlayerProfessions, Profession, Integer> profession;

        public ActionCommandTreeNode(CommandTreeNode parent, String type, BiConsumer<PlayerData, Integer> main,
                                     TriConsumer<PlayerProfessions, Profession, Integer> profession) {
            super(parent, type);

            this.main = main;
            this.profession = profession;

            addArgument(Argument.PLAYER);
            addArgument(Arguments.PROFESSION);
            addArgument(Argument.AMOUNT_INT);
        }

        @Override
        public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            if (args.length < 6)
                return CommandResult.THROW_USAGE;

            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[5]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + args[5] + " is not a valid number.");
                return CommandResult.FAILURE;
            }

            PlayerData data = PlayerData.get(player);
            if (args[4].equalsIgnoreCase("main")) {
                main.accept(data, amount);

                CommandVerbose.verbose(sender, CommandVerbose.CommandType.LEVEL, ChatColor.GOLD + player.getName()
                        + ChatColor.YELLOW + " is now Lvl " + ChatColor.GOLD + data.getLevel() + ChatColor.YELLOW + ".");
                return CommandResult.SUCCESS;
            }

            String format = args[4].toLowerCase().replace("_", "-");
            if (!MMOCore.plugin.professionManager.has(format)) {
                sender.sendMessage(ChatColor.RED + format + " is not a valid profession.");
                return CommandResult.FAILURE;
            }

            Profession profession = MMOCore.plugin.professionManager.get(format);
            this.profession.accept(data.getCollectionSkills(), profession, amount);
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.LEVEL,
                    ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " is now Lvl " + ChatColor.GOLD
                            + data.getCollectionSkills().getLevel(profession) + ChatColor.YELLOW + " in " + profession.getName() + ".");
            return CommandResult.SUCCESS;
        }
    }


    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
