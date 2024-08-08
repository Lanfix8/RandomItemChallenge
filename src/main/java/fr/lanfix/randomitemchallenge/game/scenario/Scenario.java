package fr.lanfix.randomitemchallenge.game.scenario;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Scenario {

    private final String name;

    int dropInterval;
    int dropStacks;
    int dropCount;

    Scenario(String name, int dropInterval, int dropStacks, int dropCount) {
        this.name = name;
        this.dropInterval = dropInterval;
        this.dropStacks = dropStacks;
        this.dropCount = dropCount;
    }

    public static Scenario loadScenario(RandomItemChallenge plugin, String codeName) {
        File scenarioFile = new File(plugin.getDataFolder(), codeName + ".yml");
        YamlConfiguration scenarioConfig = YamlConfiguration.loadConfiguration(scenarioFile);

        // basic parameters
        String name = scenarioConfig.getString("name", "Unnamed Scenario");

        int dropInterval = scenarioConfig.getInt("drop-interval", 2);
        int dropStacks = scenarioConfig.getInt("stacks", 9);
        int dropCount = scenarioConfig.getInt("drop-count", 1);

        // type-specific parameters
        switch (scenarioConfig.getString("type", "allItems")) {
            case "list" -> {
                List<Material> choices = new ArrayList<>();
                plugin.getConfig().getStringList("items").forEach(
                        string -> choices.add(Material.valueOf(string.toUpperCase())));
                return new OneListScenario(name, dropInterval, dropStacks, dropCount, choices);
            }
            case "allItems" -> {
                List<Material> choices = new ArrayList<>();
                for (Material material : Material.values()) if (material.isItem()) choices.add(material);
                return new OneListScenario(name, dropInterval, dropStacks, dropCount, choices);
            }
            default -> throw new IllegalStateException("Wrong itemChooseMode: " + plugin.getConfig().getString("itemChooseMode"));
        }
    }

    public abstract List<ItemStack> getNewDrop(Random random, String playerName);

    public void protectItems(List<ItemStack> items, String playerName) {
        items.forEach(item -> protectItem(item, playerName));
    }

    public void protectItem(ItemStack item, String playerName) {
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
