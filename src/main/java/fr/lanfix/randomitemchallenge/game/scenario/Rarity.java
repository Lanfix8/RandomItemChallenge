package fr.lanfix.randomitemchallenge.game.scenario;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rarity {

    private Random random = new Random();

    private final int dropStacks;
    private final int dropCount;

    private final double probability;
    private final List<Material> choices;
    private final String name;

    public Rarity(double probability, List<Material> choices, int dropStacks, int dropCount, String name) {
        this.probability = probability;
        this.choices = choices;
        this.dropStacks = dropStacks;
        this.dropCount = dropCount;
        this.name = name;
    }

    public List<ItemStack> getNewDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < dropCount; i++) {
            // Choose item
            Material material = choices.get(random.nextInt(choices.size()));
            ItemStack item = new ItemStack(material, material.getMaxStackSize());
            for (int j = 0; j < dropStacks; j++) {
                drops.add(item);
            }
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
