package net.Indyuce.mmocore.command.builtin.mmocore.admin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.TriConsumer;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class ExperienceCommandTreeNode extends CommandTreeNode {
    public ExperienceCommandTreeNode(CommandTreeNode parent) {
        super(parent, "exp");

        addChild(new CheckCommandTreeNode(this));
        addChild(new ActionCommandTreeNode(this, "set", PlayerData::setExperience, PlayerProfessions::setExperience));
        addChild(new ActionCommandTreeNode(this, "give", (data, value) -> data.giveExperience(value, EXPSource.COMMAND), (professions, profession,
                                                                                                                          value) -> professions.giveExperience(profession, value, EXPSource.COMMAND)));
        addChild(new ActionCommandTreeNode(this, "take", (data, value) -> data.giveExperience(-value, EXPSource.COMMAND), (professions, profession,
                                                                                                                           value) -> professions.giveExperience(profession, -value, EXPSource.COMMAND)));
    }

    public static class CheckCommandTreeNode extends CommandTreeNode {
        private final Argument<Player> argPlayer;
        private final Argument<Profession> argProfession;

        public CheckCommandTreeNode(CommandTreeNode parent) {
            super(parent, "check");

            argPlayer = addArgument(Argument.PLAYER);
            argProfession = addArgument(Arguments.PROFESSION.withFallback(explorer -> null));
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var player = explorer.parse(this.argPlayer);
            final @Nullable var profession = explorer.parse(this.argProfession);

            PlayerData data = PlayerData.get(player);
            final var formatter = MythicLib.plugin.getMMOConfig().decimal;

            if (profession == null)
                return explorer.success("&6" + player.getName() + "&e has &6" + formatter.format(data.getExperience()) + "&e class EXP.");

            final var currentExp = data.getCollectionSkills().getExperience(profession);
            return explorer.success("&6" + player.getName() + "&e has &6" + formatter.format(currentExp) + "&e EXP in &6" + profession.getName() + "&e.");
        }
    }

    public static class ActionCommandTreeNode extends CommandTreeNode {
        private final BiConsumer<PlayerData, Double> main;
        private final TriConsumer<PlayerProfessions, Profession, Double> profession;

        private final Argument<Player> argPlayer;
        private final Argument<Double> argAmount;
        private final Argument<Profession> argProfession;

        public ActionCommandTreeNode(CommandTreeNode parent,
                                     String type,
                                     BiConsumer<PlayerData, Double> main,
                                     TriConsumer<PlayerProfessions, Profession, Double> profession) {
            super(parent, type);

            this.main = main;
            this.profession = profession;

            argPlayer = addArgument(Argument.PLAYER);
            argProfession = addArgument(Arguments.PROFESSION);
            argAmount = addArgument(Argument.AMOUNT_DOUBLE);
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var player = explorer.parse(this.argPlayer);
            final var amount = explorer.parse(this.argAmount);
            final @Nullable var profession = explorer.parse(this.argProfession);

            PlayerData data = PlayerData.get(player);
            final var formatter = MythicLib.plugin.getMMOConfig().decimal;

            if (profession == null) {
                main.accept(data, amount);
                return explorer.success("&6" + player.getName() + "&e now has &6" + formatter.format(data.getExperience()) + "&e class EXP.");
            }

            this.profession.accept(data.getCollectionSkills(), profession, amount);
            final var currentExp = data.getCollectionSkills().getExperience(profession);
            return explorer.success("&6" + player.getName() + "&e now has &6" + formatter.format(currentExp) + "&e EXP in &6" + profession.getName() + "&e.");
        }
    }
}
