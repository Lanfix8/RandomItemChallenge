package fr.lanfix.randomitemchallenge.game.scenario;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OneListScenario extends Scenario {

    private final List<Material> choices;

    public OneListScenario(String name, int dropInterval, int dropStacks, int dropCount, List<Material> choices) {
        super(name, dropInterval, dropStacks, dropCount);
        this.choices = choices;
    }

    @Override
    public List<ItemStack> getNewDrop(Random random, String playerName) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < this.dropCount; i++) {
            // Choose item
            Material material = choices.get(random.nextInt(choices.size())); // FIXME IllegalArgumentException: bound must be positive
            ItemStack item = new ItemStack(material, material.getMaxStackSize());
            for (int j = 0; j < this.dropStacks; j++) {
                drops.add(item);
            }
        }
        // Protect items from other to pick them up
        protectItems(drops, playerName);
        return drops;
    }

}
