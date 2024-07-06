package fr.lanfix.randomitemchallenge.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public class NoScoreboard extends ScoreboardManager {

    public NoScoreboard() {
        super(null, List.of(), false);
    }

    @Override
    public void updateScoreboard(Player player) {}

    @Override
    public void newScoreboard(Player player) {}
}
