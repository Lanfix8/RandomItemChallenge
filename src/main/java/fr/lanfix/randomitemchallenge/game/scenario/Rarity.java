package fr.lanfix.randomitemchallenge.game.scenario;

import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.DropChoice;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rarity {

    private Random random = new Random();

    private final int dropCount;

    private final double probability;
    private final List<DropChoice> dropChoices;
    private final String name;

    public Rarity(double probability, List<DropChoice> dropChoices, int dropCount, String name) {
        this.probability = probability;
        this.dropChoices = dropChoices;
        this.dropCount = dropCount;
        this.name = name;
    }

    public List<ItemStack> getNewDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < dropCount; i++) {
            // Choose item
            DropChoice dropChoice = this.dropChoices.get(random.nextInt(this.dropChoices.size()));
            drops.addAll(dropChoice.getDrops());
        }
        return drops;
    }

    public String getName() {
        return name;
    }

    public double getProbability() {
        return probability;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
