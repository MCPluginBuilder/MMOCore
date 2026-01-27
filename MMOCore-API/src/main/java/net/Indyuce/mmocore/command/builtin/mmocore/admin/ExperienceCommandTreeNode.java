package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.TriConsumer;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class ExperienceCommandTreeNode extends CommandTreeNode {
    public ExperienceCommandTreeNode(CommandTreeNode parent) {
        super(parent, "exp");

        addChild(new ActionCommandTreeNode(this, "set", PlayerData::setExperience, PlayerProfessions::setExperience));
        addChild(new ActionCommandTreeNode(this, "give", (data, value) -> data.giveExperience(value, EXPSource.COMMAND), (professions, profession,
                                                                                                                          value) -> professions.giveExperience(profession, value, EXPSource.COMMAND)));
        addChild(new ActionCommandTreeNode(this, "take", (data, value) -> data.giveExperience(-value, EXPSource.COMMAND), (professions, profession,
                                                                                                                           value) -> professions.giveExperience(profession, -value, EXPSource.COMMAND)));
    }

    public static class ActionCommandTreeNode extends CommandTreeNode {
        private final BiConsumer<PlayerData, Long> main;
        private final TriConsumer<PlayerProfessions, Profession, Long> profession;

        public ActionCommandTreeNode(CommandTreeNode parent, String type, BiConsumer<PlayerData, Long> main,
                                     TriConsumer<PlayerProfessions, Profession, Long> profession) {
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
                return explorer.fail("Could not find the player called " + args[3] + ".");
            }

            long amount;
            try {
                amount = Long.parseLong(args[5]);
                Validate.isTrue(amount >= 0);
            } catch (RuntimeException exception) {
                return explorer.fail( args[5] + " is not a valid number.");
            }

            PlayerData data = PlayerData.get(player);
            if (args[4].equalsIgnoreCase("main")) {
                main.accept(data, amount);
                return explorer.success(ChatColor.GOLD + player.getName() + ChatColor.YELLOW
                        + " now has " + ChatColor.GOLD + MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()) + ChatColor.YELLOW + " EXP.");
            }

            String format = args[4].toLowerCase().replace("_", "-");
            if (!MMOCore.plugin.professionManager.has(format)) {
                return explorer.fail(format + " is not a valid profession.");
            }

            Profession profession = MMOCore.plugin.professionManager.get(format);
            this.profession.accept(data.getCollectionSkills(), profession, amount);
            return explorer.success(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " now has " + ChatColor.GOLD
                    + data.getCollectionSkills().getExperience(profession) + ChatColor.YELLOW + " EXP in " + profession.getName() + ".");
        }
    }
}
