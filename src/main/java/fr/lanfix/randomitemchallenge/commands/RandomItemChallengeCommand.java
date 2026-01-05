package fr.lanfix.randomitemchallenge.commands;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.game.scenario.Configuration;
import fr.lanfix.randomitemchallenge.game.scenario.Scenario;
import fr.lanfix.randomitemchallenge.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class RandomItemChallengeCommand implements CommandExecutor, TabCompleter {

    private final RandomItemChallenge plugin;
    private final Game game;
    private final Text text;

    public RandomItemChallengeCommand(RandomItemChallenge plugin, Game game, Text text) {
        this.plugin = plugin;
        this.game = game;
        this.text = text;
    }

    /*
    /ric start [scenario]
    /ric stop
    /ric scenario <set|setDefault> [scenario]
    /ric reload
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String msg, String[] args) {
        if (args.length == 0) {
            if (game.isRunning()) {
                Bukkit.broadcastMessage(text.getBroadcast("forced-stop"));
                game.stop();
            } else {
                game.start();
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("start")) {
            if (game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "The game is already running...");
                return true;
            }
            if (args.length == 1) {
                game.start();
            } else {
                game.start(Configuration.getScenarioOrDefault(args[1]));
            }
            return true;
        } 
        else if (args[0].equalsIgnoreCase("stop")) {
            if (!game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "The game is not running...");
            } else {
                game.stop();
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("scenario")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Invalid usage : /ric scenario <set|setDefault> [scenario]");
                return true;
            }
            if (args[1].equalsIgnoreCase("set")) {
                if (args.length == 2) {
                    sender.sendMessage(ChatColor.RED + "Please precise a scenario");
                    sender.sendMessage(ChatColor.RED + "/ric scenario set [scenario]");
                    return true;
                }
                Scenario scenario = Configuration.scenarios.get(args[2]);
                if (scenario == null) {
                    sender.sendMessage(ChatColor.RED + "Please provide a valid scenario name.");
                    sender.sendMessage(ChatColor.RED + "/ric scenario set [scenario]");
                    return true;
                }
                game.setScenario(scenario);
                sender.sendMessage(ChatColor.GREEN + "The scenario has been successfully changed to '" + scenario.getName() + "' !");
                return true;
            }
            else if (args[1].equalsIgnoreCase("setdefault")) {
                if (args.length == 2) {
                    sender.sendMessage(ChatColor.RED + "Please precise a scenario");
                    sender.sendMessage(ChatColor.RED + "/ric scenario setDefault [scenario]");
                    return true;
                }
                Scenario scenario = Configuration.scenarios.get(args[2]);
                if (scenario == null) {
                    sender.sendMessage(ChatColor.RED + "Please provide a valid scenario name.");
                    sender.sendMessage(ChatColor.RED + "/ric scenario setDefault [scenario]");
                    return true;
                }
                game.setScenario(scenario);
                Configuration.setDefaultScenario(scenario);
                plugin.getConfig().set("default-scenario", args[2]);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "The default scenario has been successfully changed to '" + scenario.getName() + "' !");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Invalid usage : /ric scenario <set|setDefault> [scenario]");
            return true;
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.GREEN + "Reloading Random Item Challenge.");
            plugin.reload();
            return true;
        }
        else return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                return Stream.of("start", "stop", "scenario", "reload")
                        .filter(arg -> arg.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("scenario")) {
                    return Stream.of("set", "setDefault")
                            .filter(arg -> arg.toLowerCase().startsWith(args[1].toLowerCase())).toList();
                }
                if (args[0].equalsIgnoreCase("start")) {
                    return Configuration.scenarios.keySet().stream()
                            .filter(arg -> arg.toLowerCase().startsWith(args[1].toLowerCase())).toList();
                }
                return List.of();
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("scenario") && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("setDefault"))) {
                    return Configuration.scenarios.keySet().stream()
                            .filter(arg -> arg.toLowerCase().startsWith(args[2].toLowerCase())).toList();
                }
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

}
