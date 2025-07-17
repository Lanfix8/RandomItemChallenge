package fr.lanfix.randomitemchallenge.game.scenario.dropchoice;

import fr.lanfix.randomitemchallenge.game.scenario.Scenario;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SingleDropChoice extends DropChoice {

    private final int dropStacks;
    private final Material material;

    public SingleDropChoice(int dropStacks, Material material) {
        this.dropStacks = dropStacks;
        this.material = material;
    }

    public SingleDropChoice(int dropStacks, String material) {
        this.dropStacks = dropStacks;
        this.material = Material.valueOf(material.toUpperCase());
    }

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack item = new ItemStack(material, material.getMaxStackSize());
        for (int j = 0; j < this.dropStacks; j++) {
            drops.add(item);
        }
        return drops;
    }
}
