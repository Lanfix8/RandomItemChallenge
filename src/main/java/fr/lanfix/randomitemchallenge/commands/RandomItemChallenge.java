package fr.lanfix.randomitemchallenge.commands;

import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RandomItemChallenge implements CommandExecutor {

    private final Game game;
    private final Text text;

    public RandomItemChallenge(Game game, Text text) {
        this.game = game;
        this.text = text;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if (game.isRunning()) {
            Bukkit.broadcastMessage(text.getBroadcast("forced-stop"));
            game.stop();
        } else {
            game.start();
        }
        return true;
    }
}
