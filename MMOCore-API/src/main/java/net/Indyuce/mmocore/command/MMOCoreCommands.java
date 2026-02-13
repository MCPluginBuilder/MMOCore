package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.command.BuiltinCommand;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.command.builtin.*;
import net.Indyuce.mmocore.command.builtin.mmocore.MMOCoreCommandTreeRoot;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;

import java.util.List;

public class MMOCoreCommands {
    public static final BuiltinCommand MMOCORE = new BuiltinCommand(true, "mmocore", MMOCoreCommandTreeRoot::new);
    public static final BuiltinCommand CAST = new BuiltinCommand("cast", "mmocore.cast", "Enter casting mode", CastCommand::new);
    public static final BuiltinCommand PLAYER = new BuiltinCommand("player", "mmocore.profile", "Displays player stats", PlayerStatsCommand::new, List.of("p", "profile"));
    public static final BuiltinCommand ATTRIBUTES = new BuiltinCommand("attributes", "mmocore.attributes", "Display and manage attributes", AttributesCommand::new, List.of("att", "stats"));
    public static final BuiltinCommand CLASS = new BuiltinCommand("class", "mmocore.class-select", "Select a new class", ClassCommand::new, List.of("c"));
    public static final BuiltinCommand WAYPOINTS = new BuiltinCommand("waypoints", "mmocore.waypoints", "Display discovered waypoints", WaypointsCommand::new, List.of("wp"));
    public static final BuiltinCommand QUESTS = new BuiltinCommand("quests", "mmocore.quests", "Display available quests", QuestsCommand::new, List.of("q", "journal"));
    public static final BuiltinCommand SKILLS = new BuiltinCommand("skills", "mmocore.skills", "Spend skill points to unlock new skills", SkillsCommand::new, List.of("s"));
    public static final BuiltinCommand FRIENDS = new BuiltinCommand("friends", "mmocore.friends", "Show online/offline friends", FriendsCommand::new, List.of("f"));
    public static final BuiltinCommand PARTY = new BuiltinCommand("party", "mmocore.party", "Invite players in a party to split exp", PartyCommand::new, () -> (MMOCore.plugin.partyModule instanceof MMOCorePartyModule), List.of());
    public static final BuiltinCommand GUILD = new BuiltinCommand("guild", "mmocore.guild", "Show players in current guild", GuildCommand::new);
    public static final BuiltinCommand WITHDRAW = new BuiltinCommand("withdraw", "mmocore.currency", "Withdraw money into coins and notes", WithdrawCommand::new, () -> MMOCore.plugin.hasEconomy() && MMOCore.plugin.economy.isValid(), List.of());
    public static final BuiltinCommand SKILL_TREES = new BuiltinCommand("skilltrees", "mmocore.skilltrees", "Open up the skill tree menu", SkillTreesCommand::new, List.of("st", "trees", "tree"));
    public static final BuiltinCommand DEPOSIT = new BuiltinCommand("deposit", "mmocore.currency", "Open the currency deposit menu", DepositCommand::new, List.of("d"));
    public static final BuiltinCommand PVP_MODE = new BuiltinCommand("pvpmode", "mmocore.pvpmode", "Toggle on/off PVP mode.", PvpModeCommand::new, List.of("pvp"));

}
