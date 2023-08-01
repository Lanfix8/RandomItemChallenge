package fr.lanfix.randomitemchallenge.events;

import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.game.GameManager;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class GameEvents implements Listener {

    private final GameManager gameManager;

    public GameEvents(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Disable hunger
    @EventHandler
    public void onLoseHunger(FoodLevelChangeEvent event) {
        Game game = gameManager.getGameWithPlayer((Player) event.getEntity());
        if (game != null && game.isRunning()) {
            event.setFoodLevel(20);
        }
    }

    // Disallow portals
    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (WorldManager.getWorldManager().isEventInGameWorld(event)) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            Game game = gameManager.getGameWithPlayer(player);
            if (game.isRunning()) {
                player.setGameMode(GameMode.SPECTATOR);  // Maybe improve the spectator when death change
                game.playerDeath(player);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getGameWithPlayer(player);
        if (game.isRunning()) {
            game.playerDeath(player);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        Game game = gameManager.getGameWithPlayer(player);
        if (game.isRunning()) {
            game.playerDeath(player);
        }
    }

}
