package fr.lanfix.randomitemchallenge.game.scenario;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RaritiesScenario extends Scenario {

    private final List<Rarity> rarities;

    public RaritiesScenario(String name, String broadcastMessage, int dropInterval, int dropStacks, int dropCount, List<Rarity> rarities) {
        super(name, broadcastMessage, dropInterval, dropStacks, dropCount);
        this.rarities = rarities;
        rarities.forEach(rarity -> rarity.setRandom(random));
    }

    @Override
    public void giveItems(List<Player> players) {
        // choose rarity
        double randomDouble = random.nextDouble();
        Rarity chosenRarity = null;
        for (Rarity rarity: rarities) {
            if (randomDouble <= rarity.getProbability()) {
                chosenRarity = rarity;
                break;
            }
            randomDouble -= rarity.getProbability();
        }
        assert chosenRarity != null;
        // broadcast
        Bukkit.broadcastMessage(broadcastMessage.replace("%rarity%", chosenRarity.getName()));
        // repeat for all players
        for (Player player: players) {
            // get drops and location
            World world = player.getWorld();
            Location location = player.getLocation();
            List<ItemStack> drops = chosenRarity.getNewDrop();
            // Protect items from other to pick them up
            protectItems(drops, player.getName());
            // Drop items
            drops.forEach(item -> world.dropItem(location, item));
        }
    }

}
