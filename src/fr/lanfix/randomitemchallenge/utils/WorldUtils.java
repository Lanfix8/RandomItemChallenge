package fr.lanfix.randomitemchallenge.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class WorldUtils {

    public static Location getSpawnHeight(World world, int x, int z) {
        int y = world.getMaxHeight() - 1;
        while (world.getBlockData(x, y, z).getMaterial().equals(Material.AIR)) y--;
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

}
