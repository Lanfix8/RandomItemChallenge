package fr.lanfix.randomitemchallenge.world;

import org.bukkit.*;
import org.bukkit.event.world.WorldEvent;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class WorldManager {

    private static WorldManager worldManager;

    private final List<String> biomesBlacklist;

    private final World world;

    private Location nextLocation;

    private WorldManager(List<String> biomesBlacklist, int border) {
        this.biomesBlacklist = biomesBlacklist;
        World world = Bukkit.getWorld("RandomItemChallenge_world");
        if (world == null) world = createWorld();
        world.getWorldBorder().setSize(border);
        world.setTime(0);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        this.world = world;
        loadNextLocation();
    }

    public static void createWorldManager(List<String> biomesBlacklist, int border) {
        worldManager = new WorldManager(biomesBlacklist, border);
    }

    public static WorldManager getWorldManager() {
        if (worldManager == null) worldManager = new WorldManager(new ArrayList<>(), 500);
        return worldManager;
    }

    private World createWorld() {
        WorldCreator worldCreator = new WorldCreator("RandomItemChallenge_world");
        return worldCreator.createWorld();
    }

    public Location getSpawnLocation() {
        Location spawnLocation = this.nextLocation;
        world.getWorldBorder().setCenter(spawnLocation.getX(), spawnLocation.getZ());
        world.setSpawnLocation(spawnLocation);
        return spawnLocation;
    }

    public void loadNextLocation() {
        int tries = 1;
        boolean found = false;
        int x = 0;
        int z = 0;
        Random random = new Random();
        while (!found) {
            if (world.getBlockAt(x, -64, z).getType().equals(Material.BEDROCK) &&
                    !biomesBlacklist.contains(world.getBiome(x, -64, z).toString())) {
                found = true;
            } else {
                x = random.nextInt(tries * -5, tries * 5 + 1) * 600;
                z = random.nextInt(tries * -5, tries * 5 + 1) * 600;
                tries++;
            }
        }
        world.setBlockData(x, -64, z, world.getBlockData(x, -50, z));
        Bukkit.getLogger().log(Level.INFO, "Found next starting location in %TRIES tries.".replace("%TRIES", String.valueOf(tries)));
        this.nextLocation = getSpawnHeight(x, z);
        preloadNextLocation();
    }

    private void preloadNextLocation() {
        ChunkyAPI api = Bukkit.getServicesManager().load(ChunkyAPI.class);
        if (api == null) return;
        boolean hasStarted = api.startTask(world.getName(), "square",
                nextLocation.getX(), nextLocation.getZ(), 20, 20, "concentric");
        if (hasStarted) Bukkit.getLogger().info("Next Random Item Challenge game area started pre-generating its chunks.");
        api.onGenerationComplete(generationCompleteEvent -> {
            if (generationCompleteEvent.world().equals(world.getName())) {
                Bukkit.getLogger().info("Next Random Item Challenge area pre-generated successfully !");
            }
        });
    }

    public void startGracePeriod() {
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        world.setPVP(false);
    }

    public void endGracePeriod() {
        world.setGameRule(GameRule.FALL_DAMAGE, true);
        world.setPVP(true);
    }

    private Location getSpawnHeight(int x, int z) {
        int y = world.getMaxHeight() - 1;
        while (world.getBlockData(x, y, z).getMaterial().equals(Material.AIR)) y--;
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    public boolean isEventInGameWorld(WorldEvent event) {
        return event.getWorld() == world;
    }

}
