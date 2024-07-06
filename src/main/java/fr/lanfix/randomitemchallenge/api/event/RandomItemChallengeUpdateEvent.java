package fr.lanfix.randomitemchallenge.api.event;

import fr.lanfix.randomitemchallenge.game.Game;

public class RandomItemChallengeUpdateEvent extends RandomItemChallengeEvent {

    public RandomItemChallengeUpdateEvent(Game game) {
        super(game);
    }

}
