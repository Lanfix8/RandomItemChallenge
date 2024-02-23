package fr.lanfix.randomitemchallenge.events;

import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class GameEvents implements Listener {

    private final Game game;

    public GameEvents(Game game) {
        this.game = game;
    }

    // Disable hunger
    @EventHandler
    public void onLoseHunger(FoodLevelChangeEvent event) {
        if (this.game.isRunning()) {
            event.setFoodLevel(20);
        }
    }

    // Disallow portals
    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (this.game.isRunning() && WorldManager.getWorldManager().isEventInGameWorld(event)) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (this.game.isRunning() && event.getEntity() instanceof Player player) {
            player.setGameMode(GameMode.SPECTATOR);  // Maybe improve the spectator when death change
            this.game.playerDeath(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (this.game.isRunning()) {
            this.game.playerDeath(event.getPlayer());
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (this.game.isRunning()) {
            this.game.playerDeath(event.getPlayer());
        }
    }

}
