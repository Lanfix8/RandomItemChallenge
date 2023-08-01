package fr.lanfix.randomitemchallenge.commands;

import fr.lanfix.randomitemchallenge.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RandomItemChallenge implements CommandExecutor {

    private final Game game;

    public RandomItemChallenge(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if (game.isRunning()) {
            Bukkit.broadcastMessage(ChatColor.RED + "Forced stop of the Random Item Challenge");
            game.stop();
        } else {
            game.start();
        }
        return true;
    }
}
