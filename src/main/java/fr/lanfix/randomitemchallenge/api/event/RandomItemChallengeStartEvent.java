package fr.lanfix.randomitemchallenge.api.event;

import fr.lanfix.randomitemchallenge.game.Game;

public class RandomItemChallengeStartEvent extends RandomItemChallengeEvent {

    public RandomItemChallengeStartEvent(Game game) {
        super(game);
    }

}
