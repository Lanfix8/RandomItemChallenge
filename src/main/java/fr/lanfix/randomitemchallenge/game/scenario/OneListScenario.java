package fr.lanfix.randomitemchallenge.game.scenario;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OneListScenario extends Scenario {

    private final List<Material> choices;

    public OneListScenario(String name, String broadcastMessage, int dropInterval, int dropStacks, int dropCount, List<Material> choices) {
        super(name, broadcastMessage, dropInterval, dropStacks, dropCount);
        this.choices = choices;
    }

    @Override
    protected List<ItemStack> getNewDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < this.dropCount; i++) {
            // Choose item
            Material material = choices.get(random.nextInt(choices.size()));
            ItemStack item = new ItemStack(material, material.getMaxStackSize());
            for (int j = 0; j < this.dropStacks; j++) {
                drops.add(item);
            }
        }
        return drops;
    }

}
