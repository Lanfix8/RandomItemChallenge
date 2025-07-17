package fr.lanfix.randomitemchallenge.game.scenario;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.exceptions.ConfigurationException;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.DropChoice;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.SingleDropChoice;
import fr.lanfix.randomitemchallenge.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public abstract class Scenario {

    final Random random;

    public static final Map<String, Scenario> scenarios = new HashMap<>();
    public static Scenario defaultScenario;

    private final String name;

    final String broadcastMessage;

    final int dropInterval;
    final int dropCount;

    Scenario(String name, String broadcastMessage, int dropInterval, int dropCount) {
        this.random = new Random();
        this.name = name;
        this.broadcastMessage = broadcastMessage;
        this.dropInterval = dropInterval;
        this.dropCount = dropCount;
    }

    public static void loadScenarios(RandomItemChallenge plugin) {
        scenarios.clear();
        File scenariosFolder = new File(plugin.getDataFolder(), "scenarios");
        String[] scenarioCodes = scenariosFolder.list(FileUtils.endsWithFilenameFilter(".yml"));
        assert scenarioCodes != null;
        for (String scenarioCode : scenarioCodes) {
            scenarioCode =  scenarioCode.replace(".yml", "");
            scenarios.put(scenarioCode, loadScenario(plugin, scenarioCode));
        }
        defaultScenario = scenarios.get(plugin.getConfig().getString("default-scenario"));
    }

    private static Scenario loadScenario(RandomItemChallenge plugin, String codeName) {
        File scenarioFile = new File(plugin.getDataFolder(),  "scenarios/" + codeName + ".yml");
        YamlConfiguration scenarioConfig = YamlConfiguration.loadConfiguration(scenarioFile);

        // basic parameters
        String name = scenarioConfig.getString("name", "Unnamed Scenario");

        String broadcastMessage = scenarioConfig.getString("item-drop-message", "§aITEM DROP !!");

        int dropInterval = scenarioConfig.getInt("drop-interval", 2);
        int dropStacks = scenarioConfig.getInt("stacks", 9);
        int dropCount = scenarioConfig.getInt("drop-count", 1);

        // type-specific parameters
        switch (scenarioConfig.getString("type", "allItems")) {
            case "list" -> {
                List<DropChoice> dropChoices = DropChoice.loadDropChoices(scenarioConfig.getStringList("items"), dropStacks);
                return new OneListScenario(name, broadcastMessage, dropInterval, dropCount, dropChoices);
            }
            case "allItems" -> {
                List<DropChoice> dropChoices = new ArrayList<>();
                for (Material material : Material.values()) if (material.isItem())
                    dropChoices.add(new SingleDropChoice(dropStacks, material));
                return new OneListScenario(name, broadcastMessage, dropInterval, dropCount, dropChoices);
            }
            case "rarities" -> {
                ConfigurationSection raritiesSection = scenarioConfig.getConfigurationSection("rarities");
                if (raritiesSection == null) throw new ConfigurationException(
                        "Rarities scenario '" + name + "' has no 'rarities' field in its configuration.");
                List<Rarity> rarities = raritiesSection.getKeys(false).stream().map(rarity -> {
                    String rarityName = raritiesSection.getString(rarity + ".name");
                    double probability = raritiesSection.getDouble(rarity + ".probability");
                    List<DropChoice> dropChoices = DropChoice.loadDropChoices(raritiesSection.getStringList(rarity + ".items"), dropStacks);
                    return new Rarity(probability, dropChoices, dropCount, rarityName);
                }).toList();
                return new RaritiesScenario(name, broadcastMessage, dropInterval, dropStacks, dropCount, rarities);
            }
            default -> throw new IllegalStateException("Wrong scenario type: " + plugin.getConfig().getString("type"));
        }
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

    public static Scenario getOrDefault(String codeName) {
        Scenario scenario = scenarios.get(codeName);
        if (scenario == null) scenario = defaultScenario;
        return scenario;
    }

    public String getName() {
        return name;
    }

    public int getDropInterval() {
        return dropInterval;
    }

    public static void setDefaultScenario(Scenario defaultScenario) {
        Scenario.defaultScenario = defaultScenario;
    }
}
