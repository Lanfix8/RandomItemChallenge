package fr.lanfix.randomitemchallenge.game;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.api.event.RandomItemChallengeStartEvent;
import fr.lanfix.randomitemchallenge.api.event.RandomItemChallengeStopEvent;
import fr.lanfix.randomitemchallenge.api.event.RandomItemChallengeUpdateEvent;
import fr.lanfix.randomitemchallenge.game.scenario.Scenario;
import fr.lanfix.randomitemchallenge.scoreboard.ScoreboardManager;
import fr.lanfix.randomitemchallenge.utils.Text;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Game {

    private final RandomItemChallenge plugin;
    private final WorldManager worldManager;
    private final Text text;
    private final ScoreboardManager sb;

    private Scenario scenario;

    private boolean running;
    private BukkitRunnable gameLoop;

    private final List<Player> players;
    private final List<Player> spectators;
    private String leaderboard;

    private final int durationHours;
    private final int durationMin;

    private int hours;
    private int min;
    private int sec;

    public Game(RandomItemChallenge plugin, Text text, ScoreboardManager sb) {
        this.plugin = plugin;
        this.text = text;
        this.sb = sb;
        this.scenario = Scenario.defaultScenario;
        this.worldManager = WorldManager.getWorldManager();
        this.running = false;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        durationHours = plugin.getConfig().getInt("game-duration.hours", 2);
        durationMin = plugin.getConfig().getInt("game-duration.minutes", 0);
    }

    public void start(Scenario scenario) {
        this.scenario = scenario;
        start();
    }

    public void start() {
        Bukkit.getPluginManager().callEvent(new RandomItemChallengeStartEvent(this));
        Bukkit.getLogger().log(Level.INFO, text.getLog("start"));
        Location spawnLocation = worldManager.getSpawnLocation();
        players.clear();
        players.addAll(Bukkit.getOnlinePlayers());
        for (Player player: players) {
            player.setHealth(20);
            player.setSaturation(20);
            player.setFoodLevel(20);
            player.setTotalExperience(0);
            player.setExp(0);
            player.sendExperienceChange(0, 0);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(spawnLocation);
            player.setRespawnLocation(spawnLocation);
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.sendTitle(text.getTitle("start"), null, -1, -1, -1);
            Tracker.trackLocation(player, player.getLocation(), text.getItem("compass"));
            sb.newScoreboard(player);
        }
        this.hours = durationHours;
        this.min = durationMin;
        this.sec = 15;
        this.running = true;
        this.leaderboard = "";
        this.gameLoop = new BukkitRunnable() {
            @Override
            public void run() {
                newGameSecond();
            }
        };
        worldManager.startGracePeriod();
        // send scenario info after 5 seconds into the game
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BLUE + "Scenario: " + scenario.getName());
            }
        }.runTaskLater(plugin, 20 + 5 * 20);
        // start game loop
        gameLoop.runTaskTimer(plugin, 20, 20);
    }

    public void stop() {
        Bukkit.getPluginManager().callEvent(new RandomItemChallengeStopEvent(this));
        this.gameLoop.cancel();
        players.forEach(player -> player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR));
        players.clear();
        spectators.forEach(player -> player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR));
        spectators.clear();
        this.running = false;
        Bukkit.broadcastMessage(text.getBroadcast("end").replace("$TIME", this.getTimeSinceStart()));
        Bukkit.broadcastMessage(ChatColor.BLUE + String.valueOf(ChatColor.UNDERLINE) + "Leaderboard :" + this.leaderboard + ChatColor.RESET);
        new BukkitRunnable() {
            @Override
            public void run() {
                worldManager.loadNextLocation();
            }
        }.runTaskLater(plugin, 5);
    }

    private void newGameSecond() {
        Bukkit.getPluginManager().callEvent(new RandomItemChallengeUpdateEvent(this));
        this.sec--;
        if (this.sec < 0) {
            // TODO Change this calculations because it is not the proper way
            if (this.min % scenario.getDropInterval() == 0) { // Drop items every x min
                scenario.giveItems(players);
            }
            this.sec += 60;
            this.min--;
            if (this.min == -1) {
                this.min += 60;
                this.hours--;
                if (this.hours == -1) { // when time runs out
                    Bukkit.broadcastMessage(text.getBroadcast("end-by-time"));
                    for (Player player : this.players) {
                        this.leaderboard = "\n%s#1) %s: Still alive%s"
                                .formatted(ChatColor.GOLD, player.getName(), this.leaderboard);
                    }
                    this.stop();
                    return;
                }
            }
            if ((this.hours == durationHours - 1 && this.durationMin == 0) ||
                    (this.hours == durationHours && this.min == durationMin - 1)) { // when grace period is over
                this.worldManager.endGracePeriod();
            }
        }
        // send scoreboard to everybody
        for (Player player: this.players) {
            sb.updateScoreboard(player);
            // update compass
            double nearestDistance = Double.POSITIVE_INFINITY;
            Player nearestEnemy = player;
            for (Player enemy: this.players) {
                if (enemy == player) continue;
                double distance = player.getLocation().distance(enemy.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEnemy = enemy;
                }
            }
            Tracker.trackLocation(player, nearestEnemy.getLocation(), text.getItem("compass"));
        }
        for (Player spectator: this.spectators) {
            sb.updateScoreboard(spectator);
        }
    }

    public void playerDeath(Player player) {
        if (this.players.contains(player)) {
            String pos = String.valueOf(this.players.size());
            Bukkit.broadcastMessage(text.getBroadcast("player-out")
                    .replace("$PLAYER", player.getName())
                    .replace("$POS", pos)
            );
            player.sendMessage(text.getMessage("time-survived").replace("$TIME", this.getTimeSinceStart()));
            this.leaderboard = "\n" + ChatColor.GREEN + "#$POS) $PLAYER: $TIME"
                    .replace("$POS", pos)
                    .replace("$PLAYER", player.getName())
                    .replace("$TIME", this.getTimeSinceStart())
                    + this.leaderboard;
            this.players.remove(player);
            this.spectators.add(player);
            if (this.players.size() == 1) {
                Bukkit.broadcastMessage(text.getBroadcast("winner")
                        .replace("$WINNER", this.players.get(0).getName())
                );
                this.leaderboard = "\n" + ChatColor.GOLD + "#1) $WINNER: Still alive"
                        .replace("$WINNER", this.players.get(0).getName())
                                + this.leaderboard;
                this.stop();
            } else if (this.players.isEmpty()) {
                Bukkit.broadcastMessage(text.getBroadcast("playing-alone"));
                this.stop();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPlayersRemaining() {
        return players.size();
    }

    public String getTimeSinceStart() {
        return "$HOURS hours and $MIN minutes."
                .replace("$HOURS", String.valueOf(this.min == 0 ? 2 - this.hours: 1 - this.hours))
                .replace("$MIN", String.valueOf(this.min == 0 ? 0: 60 - this.min));
    }

    public String getTimeRemaining() {
        return hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

}
