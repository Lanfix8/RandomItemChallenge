package fr.lanfix.randomitemchallenge.api.event;

import fr.lanfix.randomitemchallenge.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RandomItemChallengeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;

    public RandomItemChallengeEvent(Game game) {
        this.game = game;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Game getGame() {
        return game;
    }

}
