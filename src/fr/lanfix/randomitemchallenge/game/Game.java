package fr.lanfix.randomitemchallenge.game;

import fr.lanfix.randomitemchallenge.Main;
import fr.lanfix.randomitemchallenge.ScoreboardManager;
import fr.lanfix.randomitemchallenge.Tracker;
import fr.lanfix.randomitemchallenge.utils.WorldUtils;
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

    private final Main main;

    private final Random random;

    private boolean running;
    private BukkitRunnable gameLoop;

    private final List<Player> players;
    private final List<Player> spectators;
    private String leaderboard;

    private int hours;
    private int min;
    private int sec;

    public Game(Main main) {
        this.main = main;
        this.random = new Random();
        this.running = false;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
    }

    public void start() {
        Bukkit.getLogger().log(Level.INFO, "Starting Random Item Challenge");
        World world = Bukkit.getWorld("world");
        assert world != null;
        Location spawnLocation = this.getSpawnLocation(world);
        world.getWorldBorder().setSize(main.getConfig().getInt("border", 500));
        world.getWorldBorder().setCenter(spawnLocation.getX(), spawnLocation.getZ());
        world.setTime(0);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        new BukkitRunnable() {
            @Override
            public void run() {
                world.setGameRule(GameRule.FALL_DAMAGE, true);
            }
        }.runTaskLater(main, 20 * 15);
        world.setSpawnLocation(spawnLocation);
        this.leaderboard = "";
        players.clear();
        players.addAll(Bukkit.getOnlinePlayers());
        for (Player player: players) {
            player.setHealth(20);
            player.setSaturation(20);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(spawnLocation);
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.sendTitle("PvP disabled for 15 seconds", null, -1, -1, -1);
            Tracker.trackLocation(player, player.getLocation(), "Nearest Enemy");
            ScoreboardManager.newScoreboard(player, 2, 0, 15, this.players.size());
        }
        this.hours = 2;
        this.min = 0;
        this.sec = 15;
        this.running = true;
        this.gameLoop = new BukkitRunnable() {
            @Override
            public void run() {
                newGameSecond();
            }
        };
        this.gameLoop.runTaskTimer(main, 20, 20);
    }

    private Location getSpawnLocation(World world) {
        int tries = 1;
        boolean found = false;
        int x = 0;
        int z = 0;
        Random random = new Random();
        while (!found) {
            if (world.getBlockAt(x, -64, z).getType().equals(Material.BEDROCK) &&
                    !this.main.getConfig().getStringList("biomes-blacklist")
                            .contains(world.getBiome(x, -64, z).toString())) {
                found = true;
            } else {
                x = random.nextInt(-tries, tries + 1) * 600;
                z = random.nextInt(-tries, tries + 1) * 600;
                tries++;
            }
        }
        world.setBlockData(x, -64, z, world.getBlockData(x, -50, z));
        Bukkit.getLogger().log(Level.INFO, "Found starting location in %TRIES tries.".replace("%TRIES", String.valueOf(tries)));
        return WorldUtils.getSpawnHeight(world, x, z);
    }

    public void stop() {
        this.gameLoop.cancel();
        World world = Bukkit.getWorld("world");
        assert world != null;
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true);
        world.setGameRule(GameRule.DO_MOB_LOOT, true);
        world.setGameRule(GameRule.DO_TILE_DROPS, true);
        world.setGameRule(GameRule.FALL_DAMAGE, true);
        for (Player player: world.getPlayers()) {
            player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        }
        players.clear();
        this.running = false;
        Bukkit.broadcastMessage(ChatColor.BLUE + "End of the game !\n" +
                "The game lasted $TIMESINCESTART.".replace("$TIMESINCESTART", this.getTimeSinceStart())
        );
        Bukkit.broadcastMessage(ChatColor.BLUE + String.valueOf(ChatColor.UNDERLINE) + "Leaderboard :" + this.leaderboard + ChatColor.RESET);
    }

    private void newGameSecond() {
        this.sec--;
        if (this.sec == -1) {
            if (this.min % main.getConfig().getInt("drop-interval", 2) == 0) { // Drop items every x min
                this.giveItems();
            }
            this.sec += 60;
            this.min--;
            if (this.min == -1) {
                this.min += 60;
                this.hours--;
                if (this.hours == -1) { // when time runs out
                    Bukkit.broadcastMessage(ChatColor.RED + "Time has run out !\nRandom Item Challenge is over.");
                    for (Player player : this.players) {
                        this.leaderboard = "\n" + ChatColor.GOLD + "#1) $PLAYER: Still alive"
                                .replace("$PLAYER", player.getName())
                                + this.leaderboard;
                    }
                    this.stop();
                    return;
                }
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
            Tracker.trackLocation(player, nearestEnemy.getLocation(), "Nearest Enemy");
        }
        for (Player spectator: this.spectators) {
            ScoreboardManager.updateScoreboard(spectator, this.hours, this.min, this.sec, this.players.size());
        }
    }

    public void giveItems() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "ITEM DROP !!");
        List<Material> choices = switch(main.getConfig().getString("itemChooseMode", "custom")) {
            case "custom" -> {
                List<Material> r = new ArrayList<>();
                main.getConfig().getStringList("items").forEach(string -> r.add(Material.valueOf(string)));
                yield r;
            }
            case "allItems" -> {
                List<Material> r = new ArrayList<>();
                for (Material material : Material.values()) if (material.isItem()) r.add(material);
                yield r;
            }
            default -> throw new IllegalStateException("Wrong itemChooseMode: " + main.getConfig().getString("itemChooseMode"));
        };
        // repeat for all players
        for (Player player: players) {
            for (int i = 0; i < main.getConfig().getInt("drop-count", 1); i++) {
                // find the location and choose item
                Location spawnLocation = player.getLocation();
                Material material = choices.get(this.random.nextInt(choices.size()));
                ItemStack item = new ItemStack(material, material.getMaxStackSize());
                // set player name in lore to set property so others don't pick up his items
                ItemMeta itemMeta = item.getItemMeta();
                assert itemMeta != null;
                itemMeta.setLore(Collections.singletonList(player.getName()));
                item.setItemMeta(itemMeta);
                // drop the items
                for (int j = 0; j < main.getConfig().getInt("stacks", 9); j++) {
                    spawnLocation.getWorld().dropItem(spawnLocation, item);
                }
            }
        }
    }

    public void playerDeath(Player player) {
        if (this.players.contains(player)) {
            String pos = String.valueOf(this.players.size());
            Bukkit.broadcastMessage("$PLAYER is out ! (#$POS)"
                    .replace("$PLAYER", player.getName())
                    .replace("$POS", pos)
            );
            player.sendMessage(ChatColor.GREEN + "You survived $TIME !".replace("$TIME", this.getTimeSinceStart()));
            this.leaderboard = "\n" + ChatColor.GREEN + "#$POS) $PLAYER: $TIME"
                    .replace("$POS", pos)
                    .replace("$PLAYER", player.getName())
                    .replace("$TIME", this.getTimeSinceStart())
                    + this.leaderboard;
            this.players.remove(player);
            this.spectators.add(player);
            if (this.players.size() == 1) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "$WINNER has won !"
                        .replace("$WINNER", this.players.get(0).getName())
                );
                this.leaderboard = "\n" + ChatColor.GOLD + "#1) $WINNER: Still alive"
                        .replace("$WINNER", this.players.get(0).getName())
                                + this.leaderboard;
                this.stop();
            } else if (this.players.size() < 1) {
                Bukkit.broadcastMessage(ChatColor.RED + "Something wierd happened, are you damn playing alone ?");
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

    public int getHours() {
        return hours;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

}
