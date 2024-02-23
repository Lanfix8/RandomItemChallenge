package fr.lanfix.randomitemchallenge.placeholderapi;

import fr.lanfix.randomitemchallenge.game.Game;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class RandomItemChallengeExpansion extends PlaceholderExpansion {

    private final Game game;

    public RandomItemChallengeExpansion(Game game) {
        this.game = game;
    }

    @Override
    public String getAuthor() {
        return "Lanfix";
    }

    @Override
    public String getIdentifier() {
        return "randomitemchallenge";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("time")) {
            int hours = game.getHours();
            int min = game.getMinutes();
            int sec = game.getSeconds();
            return hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
        }
        else if (params.equalsIgnoreCase("players")) {
            return String.valueOf(game.getPlayersRemaining());
        }
        return null;
    }
}
