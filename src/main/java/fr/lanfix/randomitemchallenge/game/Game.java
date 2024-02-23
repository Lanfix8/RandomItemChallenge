package fr.lanfix.randomitemchallenge.game;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.scoreboard.ScoreboardManager;
import fr.lanfix.randomitemchallenge.utils.Text;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class Game {

    private final RandomItemChallenge plugin;
    private final WorldManager worldManager;
    private final Text text;

    private final Random random;

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

    public Game(RandomItemChallenge plugin, Text text) {
        this.plugin = plugin;
        this.text = text;
        this.worldManager = WorldManager.getWorldManager();
        this.random = new Random();
        this.running = false;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        durationHours = plugin.getConfig().getInt("game-duration.hours", 2);
        durationMin = plugin.getConfig().getInt("game-duration.minutes", 0);
    }

    public void start() {
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
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.sendTitle(text.getTitle("start"), null, -1, -1, -1);
            Tracker.trackLocation(player, player.getLocation(), text.getItem("compass"));
            ScoreboardManager.newScoreboard(player, 2, 0, 15, this.players.size());
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
        gameLoop.runTaskTimer(plugin, 20, 20);
    }

    public void stop() {
        this.gameLoop.cancel();
        players.forEach(player -> player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR));
        players.clear();
        spectators.forEach(player -> player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR));
        spectators.clear();
        this.running = false;
        Bukkit.broadcastMessage(text.getBroadcast("end").replace("$TIME", this.getTimeSinceStart())
        );
        Bukkit.broadcastMessage(ChatColor.BLUE + String.valueOf(ChatColor.UNDERLINE) + "Leaderboard :" + this.leaderboard + ChatColor.RESET);
        new BukkitRunnable() {
            @Override
            public void run() {
                worldManager.loadNextLocation();
            }
        }.runTaskLater(plugin, 5);
    }

    private void newGameSecond() {
        this.sec--;
        if (this.sec == -1) {
            if (this.min % plugin.getConfig().getInt("drop-interval", 2) == 0) { // Drop items every x min
                this.giveItems();
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
            ScoreboardManager.updateScoreboard(player, this.hours, this.min, this.sec, this.players.size());
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
            ScoreboardManager.updateScoreboard(spectator, this.hours, this.min, this.sec, this.players.size());
        }
    }

    public void giveItems() {
        Bukkit.broadcastMessage(text.getBroadcast("item-drop"));
        List<Material> choices = switch(plugin.getConfig().getString("itemChooseMode", "custom")) {
            case "custom" -> {
                List<Material> r = new ArrayList<>();
                plugin.getConfig().getStringList("items").forEach(string -> r.add(Material.valueOf(string.toUpperCase())));
                yield r;
            }
            case "allItems" -> {
                List<Material> r = new ArrayList<>();
                for (Material material : Material.values()) if (material.isItem()) r.add(material);
                yield r;
            }
            default -> throw new IllegalStateException("Wrong itemChooseMode: " + plugin.getConfig().getString("itemChooseMode"));
        };
        // repeat for all players
        for (Player player: players) {
            for (int i = 0; i < plugin.getConfig().getInt("drop-count", 1); i++) {
                // find the location and choose item
                Location location = player.getLocation();
                Material material = choices.get(this.random.nextInt(choices.size()));
                ItemStack item = new ItemStack(material, material.getMaxStackSize());
                // set player name in lore to set property so others don't pick up his items
                ItemMeta itemMeta = item.getItemMeta();
                assert itemMeta != null;
                itemMeta.setLore(Collections.singletonList(player.getName()));
                item.setItemMeta(itemMeta);
                // drop the items
                for (int j = 0; j < plugin.getConfig().getInt("stacks", 9); j++) {
                    player.getWorld().dropItem(location, item);
                }
            }
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

    public String getTimeSinceStart() {
        return "$HOURS hours and $MIN minutes."
                .replace("$HOURS", String.valueOf(this.min == 0 ? 2 - this.hours: 1 - this.hours))
                .replace("$MIN", String.valueOf(this.min == 0 ? 0: 60 - this.min));
    }

    public boolean isRunning() {
        return running;
    }

    public int getPlayersRemaining() {
        return players.size();
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return min;
    }

    public int getSeconds() {
        return sec;
    }

}
