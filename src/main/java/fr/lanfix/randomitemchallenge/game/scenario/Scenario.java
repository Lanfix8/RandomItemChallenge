package fr.lanfix.randomitemchallenge.game.scenario;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public abstract class Scenario {

    private final String name;

    final String broadcastMessage;

    final int dropInterval;
    final int dropCount;

    Scenario(String name, String broadcastMessage, int dropInterval, int dropCount) {
        this.name = name;
        this.broadcastMessage = broadcastMessage;
        this.dropInterval = dropInterval;
        this.dropCount = dropCount;
    }

    protected List<ItemStack> getNewDrop() {
        return List.of();
    }

    public void giveItems(List<Player> players) {
        Bukkit.broadcastMessage(broadcastMessage);
        // repeat for all players
        for (Player player: players) {
            // get drops and location
            World world = player.getWorld();
            Location location = player.getLocation();
            List<ItemStack> drops = getNewDrop();
            // Protect items from other to pick them up
            protectItems(drops, player.getName());
            // Drop items
            drops.forEach(item -> world.dropItem(location, item));
        }
    }

    protected void protectItems(List<ItemStack> items, String playerName) {
        items.forEach(item -> protectItem(item, playerName));
    }

    private void protectItem(ItemStack item, String playerName) {
        // set player name in lore to set property so others don't pick up his items
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setLore(Collections.singletonList(playerName));
        item.setItemMeta(itemMeta);
    }

    public String getName() {
        return name;
    }

    public int getDropInterval() {
        return dropInterval;
    }

}
