package fr.lanfix.randomitemchallenge.game.scenario;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.exceptions.ConfigurationException;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.AnyDropChoice;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.CustomDropChoice;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.DropChoice;
import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.SingleDropChoice;
import fr.lanfix.randomitemchallenge.utils.FileUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

    public static final Map<String, DropChoice> dropChoices = new HashMap<>();

    public static final Map<String, Scenario> scenarios = new HashMap<>();
    public static Scenario defaultScenario;

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
                List<DropChoice> dropChoices = loadDropChoices(plugin, scenarioConfig.getStringList("items"), dropStacks);
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
                        "Rarities scenario '" + name + "' has no 'rarities' field in its Configuration.");
                List<Rarity> rarities = raritiesSection.getKeys(false).stream().map(rarity -> {
                    String rarityName = raritiesSection.getString(rarity + ".name");
                    double probability = raritiesSection.getDouble(rarity + ".probability");
                    List<DropChoice> dropChoices = loadDropChoices(plugin, raritiesSection.getStringList(rarity + ".items"), dropStacks);
                    return new Rarity(probability, dropChoices, dropCount, rarityName);
                }).toList();
                return new RaritiesScenario(name, broadcastMessage, dropInterval, dropCount, rarities);
            }
            default -> throw new IllegalStateException("Wrong scenario type: " + plugin.getConfig().getString("type"));
        }
    }

    public static List<DropChoice> loadDropChoices(RandomItemChallenge plugin, List<String> dropCodes, int dropStacks) {
        List<DropChoice> dropChoices = new ArrayList<>();
        dropCodes.forEach(code -> {
            code = code.toUpperCase();
            if (code.startsWith("ANY.")) {
                dropChoices.add(loadAnyDropChoice(plugin, code, dropStacks));
            } else if (code.startsWith("CUSTOM.")) {
                dropChoices.add(loadCustomDropChoice(plugin, code));
            } else {
                dropChoices.add(loadSingleDropChoice(code, dropStacks));
            }
        });
        return dropChoices;
    }

    public static DropChoice loadSingleDropChoice(String code, int dropStacks) {
        String fullCode = code + "." + dropStacks;
        if (dropChoices.containsKey(fullCode)) {
            return dropChoices.get(fullCode);
        }
        SingleDropChoice dropChoice = new SingleDropChoice(dropStacks, code);
        dropChoices.put(fullCode, dropChoice);
        return dropChoice;
    }

    public static DropChoice loadAnyDropChoice(RandomItemChallenge plugin, String code, int dropStacks) {
        String fullCode = code + "." + dropStacks;
        if (dropChoices.containsKey(fullCode)) {
            return dropChoices.get(fullCode);
        }
        DropChoice dropChoice = new AnyDropChoice(plugin, code, dropStacks);
        dropChoices.put(fullCode, dropChoice);
        return dropChoice;
    }

    public static DropChoice loadCustomDropChoice(RandomItemChallenge plugin, String code) {
        if (dropChoices.containsKey(code)) {
            return dropChoices.get(code);
        }
        DropChoice dropChoice = new CustomDropChoice(plugin, code);
        dropChoices.put(code, dropChoice);
        return dropChoice;
    }

    public static Scenario getScenarioOrDefault(String codeName) {
        Scenario scenario = scenarios.get(codeName);
        if (scenario == null) scenario = defaultScenario;
        return scenario;
    }

    public static void setDefaultScenario(Scenario defaultScenario) {
        Configuration.defaultScenario = defaultScenario;
    }

}
