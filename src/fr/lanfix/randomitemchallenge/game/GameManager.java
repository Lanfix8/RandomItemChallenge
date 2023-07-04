package fr.lanfix.randomitemchallenge.game;

import fr.lanfix.randomitemchallenge.Main;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    // TODO Delete game when stopping it
    private final List<Game> games = new ArrayList<>();
    private final Main main;

    public GameManager(Main main) {
        this.main = main;
    }

    public Game newGame() {
        Game game = new Game(main);
        this.games.add(game);
        return game;
    }

    public Game getGameWithPlayer(Player player) {
        return games.stream().filter(game -> game.getPlayers().contains(player)).findFirst().orElse(null);
    }

    public void stopAllGames() {
        games.forEach(game -> {
            if (game.isRunning()) game.stop();
        });
    }

}
