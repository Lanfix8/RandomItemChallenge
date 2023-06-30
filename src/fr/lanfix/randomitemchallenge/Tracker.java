package fr.lanfix.randomitemchallenge;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class Tracker {

    public static boolean updateCompass(ItemStack item, Location location) {
        // this method updates the given item
        if (item.getType().equals(Material.COMPASS)) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            assert meta != null;
            meta.setLodestone(location);
            meta.setLodestoneTracked(false);
            item.setItemMeta(meta);
            return true;
        }
        return false;
    }

    public static ItemStack getCompass(Location location, String name) {
        // this method returns a compass tracking to the given location
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        meta.setLodestone(location);
        meta.setLodestoneTracked(false);
        item.setItemMeta(meta);
        return item;
    }

    public static void trackLocation(Player player, Location location, String name) {
        // this method either updates the players compass or gives a new compass to the player
        Inventory inv = player.getInventory();
        if (inv.contains(Material.COMPASS)) {
            ItemStack item = inv.getItem(inv.first(Material.COMPASS));
            assert item != null;
            // TODO Only update compass with the right name
            updateCompass(item, location);
        } else {
            int firstEmptySlot = inv.firstEmpty();
            ItemStack item = getCompass(location, name);
            if (firstEmptySlot == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                inv.setItem(firstEmptySlot, item);
            }
        }
    }

}
