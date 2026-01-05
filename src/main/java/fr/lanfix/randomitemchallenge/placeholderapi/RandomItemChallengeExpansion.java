package fr.lanfix.randomitemchallenge.placeholderapi;

import fr.lanfix.randomitemchallenge.game.Game;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class RandomItemChallengeExpansion extends PlaceholderExpansion {

    private final Game game;

    public RandomItemChallengeExpansion(Game game) {
        this.game = game;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lanfix";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "randomitemchallenge";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("time")) {
            return game.getTime();
        }
        else if (params.equalsIgnoreCase("players")) {
            return String.valueOf(game.getPlayersRemaining());
        }
        else if (params.equalsIgnoreCase("scenario")) {
            return game.getScenario().getName();
        }
        return null;
    }
}
