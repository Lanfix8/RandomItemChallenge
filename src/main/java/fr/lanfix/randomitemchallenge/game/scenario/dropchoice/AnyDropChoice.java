package fr.lanfix.randomitemchallenge.game.scenario.dropchoice;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AnyDropChoice extends DropChoice {

    private final int dropStacks;
    private final List<Material> materials;

    public AnyDropChoice(RandomItemChallenge plugin, String code, int dropStacks) {
        this.dropStacks = dropStacks;

        File dropFile = new File(plugin.getDataFolder(),
                "custom_drops/" + code.toLowerCase().replace(".", "/") + ".yml");
        YamlConfiguration dropConfig = YamlConfiguration.loadConfiguration(dropFile);

        this.materials = dropConfig.getStringList("items").stream().map(
                material -> Material.valueOf(material.toUpperCase())
        ).toList();
    }

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        Material material = this.materials.get(ThreadLocalRandom.current().nextInt(this.materials.size()));
        ItemStack item = new ItemStack(material, material.getMaxStackSize());
        for (int j = 0; j < this.dropStacks; j++) {
            drops.add(item);
        }
        return drops;
    }

}
