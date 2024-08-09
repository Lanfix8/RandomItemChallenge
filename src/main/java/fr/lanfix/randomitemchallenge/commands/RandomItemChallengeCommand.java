package fr.lanfix.randomitemchallenge.commands;

import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RandomItemChallengeCommand implements CommandExecutor {

    private final Game game;
    private final Text text;

    public RandomItemChallengeCommand(Game game, Text text) {
        this.game = game;
        this.text = text;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String msg, String[] args) {
        // TODO Make it change scenarios (add arguments and TabCompleter)
        // TODO reload cmd
        if (game.isRunning()) {
            Bukkit.broadcastMessage(text.getBroadcast("forced-stop"));
            game.stop();
        } else {
            game.start();
        }
        return true;
    }
}
