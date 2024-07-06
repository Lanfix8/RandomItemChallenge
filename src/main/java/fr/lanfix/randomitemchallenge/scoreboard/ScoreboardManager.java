package fr.lanfix.randomitemchallenge.scoreboard;

import fr.lanfix.randomitemchallenge.game.Game;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Objects;

public class ScoreboardManager {

    private final Game game;
    private final List<String> lines;
    private final boolean PAPIEnabled;

    public ScoreboardManager(Game game, List<String> lines, boolean papiEnabled) {
        this.game = game;
        this.lines = lines;
        PAPIEnabled = papiEnabled;
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();

        Objective objective = scoreboard.getObjective("RandomItemChallenge");
        if (objective == null) {
            newScoreboard(player);
            return;
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = Math.min(14, lines.size() - 1); i >= 0; i--) {
            String text = lines.get(i);
            if (PAPIEnabled) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } else {
                text = text.replace("%randomitemchallenge_time%", game.getTimeRemaining());
                text = text.replace("%randomitemchallenge_players%", String.valueOf(game.getPlayersRemaining()));
            }
            String name = ChatColor.values()[i].toString() + ChatColor.RESET;
            Team team = scoreboard.getTeam(name);
            assert team != null;
            team.setPrefix(text);
        }

        player.setScoreboard(scoreboard);
    }

    public void newScoreboard(Player player) {
        // Create new scoreboard
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("RandomItemChallenge", Criteria.DUMMY, ChatColor.GOLD + "Random Item Challenge");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = Math.min(14, lines.size() - 1); i >= 0; i--) {
            String text = lines.get(i);
            if (PAPIEnabled) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } else {
                text = text.replace("%randomitemchallenge_time%", game.getTimeRemaining());
                text = text.replace("%randomitemchallenge_players%", String.valueOf(game.getPlayersRemaining()));
            }
            String name = ChatColor.values()[i].toString() + ChatColor.RESET;
            if (scoreboard.getTeam(name) == null) scoreboard.registerNewTeam(name);
            Team team = scoreboard.getTeam(name);
            assert team != null;
            team.addEntry(name);
            team.setPrefix(text);
            objective.getScore(name).setScore(0);
        }

        player.setScoreboard(scoreboard);
    }

}
