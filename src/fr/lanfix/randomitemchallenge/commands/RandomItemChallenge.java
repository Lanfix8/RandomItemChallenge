package fr.lanfix.randomitemchallenge.commands;

import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomItemChallenge implements CommandExecutor {

    private final GameManager gameManager;

    public RandomItemChallenge(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if (sender instanceof Player player) {
            Game game = gameManager.getGameWithPlayer(player);
            if (game != null && game.isRunning()) {
                Bukkit.broadcastMessage(ChatColor.RED + "Forced stop of the Random Item Challenge");
                game.stop();
            }
        }
        gameManager.newGame().start();
        return true;
    }
}
