package fr.lanfix.randomitemchallenge.world;

import org.bukkit.GameRule;

public class GameRules {

    @SuppressWarnings("unchecked")
    public static final GameRule<Boolean> SHOW_ADVANCEMENT_MESSAGES = (GameRule<Boolean>) resolve(
            "SHOW_ADVANCEMENT_MESSAGES",
            "ANNOUNCE_ADVANCEMENTS"
    );
    @SuppressWarnings("unchecked")
    public static final GameRule<Boolean> ENTITY_DROPS = (GameRule<Boolean>) resolve(
            "ENTITY_DROPS",
            "DO_ENTITY_DROPS"
    );
    @SuppressWarnings("unchecked")
    public static final GameRule<Boolean> MOB_DROPS = (GameRule<Boolean>) resolve(
            "MOB_DROPS",
            "DO_MOB_LOOT"
    );
    @SuppressWarnings("unchecked")
    public static final GameRule<Boolean> BLOCK_DROPS = (GameRule<Boolean>) resolve(
            "BLOCK_DROPS",
            "DO_TILE_DROPS"
    );

    private static GameRule<?> resolve(String... candidates) {
        for (String name : candidates) {
            try {
                return (GameRule<?>) GameRule.class.getField(name).get(null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {

            }
        }
        throw new UnresolvedGameRuleException("No matching gamerule for : " + String.join(", ", candidates));
    }

    public static class UnresolvedGameRuleException extends RuntimeException {

        public UnresolvedGameRuleException(String message) {
            super(message);
        }

    }

}
