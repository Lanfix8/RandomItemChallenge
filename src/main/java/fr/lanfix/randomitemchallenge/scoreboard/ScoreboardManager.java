package fr.lanfix.randomitemchallenge.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    public static void updateScoreboard(Player player, int hours, int min, int sec, int playersRemaining) {
        Scoreboard scoreboard = player.getScoreboard();

        Objective objective = scoreboard.getObjective("RandomItemChallenge");
        if (objective == null) {
            newScoreboard(player, hours, min, sec, playersRemaining);
            return;
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        scoreboard.getTeam("time").setSuffix(hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
        scoreboard.getTeam("remaining").setSuffix(String.valueOf(playersRemaining));

        player.setScoreboard(scoreboard);
    }

    public static void newScoreboard(Player player, int hours, int min, int sec, int playersRemaining) {
        // Create new scoreboard
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("RandomItemChallenge", "dummy", ChatColor.GOLD + "Random Item Challenge");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // time part
        if (scoreboard.getTeam("time") == null) scoreboard.registerNewTeam("time");
        scoreboard.getTeam("time").addEntry(ChatColor.LIGHT_PURPLE + "Time remaining: ");
        scoreboard.getTeam("time").setSuffix(hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
        objective.getScore(ChatColor.LIGHT_PURPLE + "Time remaining: ").setScore(15);
        // add blank
        if (scoreboard.getTeam("blank") == null) scoreboard.registerNewTeam("blank");
        scoreboard.getTeam("blank").addEntry("   ");
        objective.getScore("   ").setScore(14);
        // players remaining
        if (scoreboard.getTeam("remaining") == null) scoreboard.registerNewTeam("remaining");
        scoreboard.getTeam("remaining").addEntry(ChatColor.RED + "Players remaining: ");
        scoreboard.getTeam("remaining").setSuffix(String.valueOf(playersRemaining));
        objective.getScore(ChatColor.RED + "Players remaining: ").setScore(13);
        // set player scoreboard
        player.setScoreboard(scoreboard);
    }

}
