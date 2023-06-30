package fr.lanfix.randomitemchallenge.events;

import fr.lanfix.randomitemchallenge.Game;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEvents implements Listener {

    private Game game;

    public GameEvents(Game game) {
        this.game = game;
    }

    // Disable hunger
    @EventHandler
    public void onLoseHunger(FoodLevelChangeEvent event) {
        if (this.game.isRunning()) {
            event.getEntity().setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFight(EntityDamageByEntityEvent event) {
        if (this.game.isRunning() && event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (this.game.getHours() == 2) {
                event.setCancelled(true);
            }
        }
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
