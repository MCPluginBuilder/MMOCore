package net.Indyuce.mmocore.command.builtin.mmocore.booster;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.Arguments;
import net.Indyuce.mmocore.experience.Booster;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CreateCommandTreeNode extends CommandTreeNode {
    private final Argument<Profession> argProfession;
    private final Argument<Double> argAmount;
    private final Argument<Long> argDuration;
    private final Argument<String> argAuthor;

    public CreateCommandTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        argProfession = addArgument(Arguments.PROFESSION);
        argAmount = addArgument(Argument.AMOUNT_DOUBLE.withAutoComplete((explorer, list) -> list.addAll(Arrays.asList("0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1"))));
        argDuration = addArgument(Argument.DURATION_TICKS);
        argAuthor = addArgument(Argument.STRING.withKey("author").withFallback(explorer -> null));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var profession = explorer.parse(argProfession);
        //final var target = explorer.parse(argPlayer);
        final var extra = explorer.parse(argAmount);
        final var length = explorer.parse(argDuration);
        final var author = explorer.parse(argAuthor);

        var multFormatted = MythicLib.plugin.getMMOConfig().decimal.format(1 + extra);
        if (profession == null) {
            MMOCore.plugin.boosterManager.register(new Booster(author, extra, length));
            Message.NEW_EXP_BOOSTER_MAIN.send(Bukkit.getOnlinePlayers(), "multiplier", multFormatted);
            return CommandResult.SUCCESS;
        }

        MMOCore.plugin.boosterManager.register(new Booster(author, profession, extra, length));
        Message.NEW_EXP_BOOSTER_PROFESSION.send(Bukkit.getOnlinePlayers(), "multiplier", multFormatted, "profession", profession.getName());
        return CommandResult.SUCCESS;
    }
}
