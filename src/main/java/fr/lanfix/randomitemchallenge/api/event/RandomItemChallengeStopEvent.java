package fr.lanfix.randomitemchallenge.api.event;

import fr.lanfix.randomitemchallenge.game.Game;

public class RandomItemChallengeStopEvent extends RandomItemChallengeEvent {

    public RandomItemChallengeStopEvent(Game game) {
        super(game);
    }

}
