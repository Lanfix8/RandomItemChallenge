package fr.lanfix.randomitemchallenge;

import fr.lanfix.randomitemchallenge.commands.RandomItemChallengeCommand;
import fr.lanfix.randomitemchallenge.events.GameEvents;
import fr.lanfix.randomitemchallenge.events.ItemEvents;
import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.game.scenario.Scenario;
import fr.lanfix.randomitemchallenge.placeholderapi.RandomItemChallengeExpansion;
import fr.lanfix.randomitemchallenge.scoreboard.NoScoreboard;
import fr.lanfix.randomitemchallenge.scoreboard.ScoreboardManager;
import fr.lanfix.randomitemchallenge.utils.Text;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public final class RandomItemChallenge extends JavaPlugin {

    private Game game;
    private Text text;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadTexts();
        this.loadGame();
        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GameEvents(game), this);
        pluginManager.registerEvents(new ItemEvents(game), this);
        // Register commands
        PluginCommand RICCommand = getCommand("randomitemchallenge");
        assert RICCommand != null;
        RICCommand.setExecutor(new RandomItemChallengeCommand(this, this.game, this.text));
        // Register PlaceholderAPI expansion
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RandomItemChallengeExpansion(this.game).register();
        }
    }

    private void loadTexts() {
        File textsFile = new File(this.getDataFolder(), "texts.yml");
        if (!textsFile.exists()) {
            InputStream link = this.getResource("texts.yml");
            assert link != null;
            try {
                Files.copy(link, textsFile.getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.text = new Text(YamlConfiguration.loadConfiguration(textsFile));
    }

    private void loadGame() {
        // load scoreboard
        File scoreboardFile = new File(this.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            InputStream link = this.getResource("scoreboard.yml");
            assert link != null;
            try {
                Files.copy(link, scoreboardFile.getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        YamlConfiguration sbConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        boolean customScoreboard = sbConfig.getBoolean("custom-scoreboard");
        boolean PAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        ScoreboardManager sb = customScoreboard ?
                new ScoreboardManager(game, sbConfig.getStringList("scoreboard"), PAPIEnabled) : new NoScoreboard();
        // load worldmanager
        WorldManager.createWorldManager(getConfig().getStringList("biomes-blacklist"),
                getConfig().getInt("border", 500),
                getConfig().getBoolean("use-default-world", true));
        // load scenarios
        Scenario.loadScenarios(this);
        if (Scenario.defaultScenario == null) Bukkit.getLogger().warning("[RandomItemChallenge] Unknown default scenario, please edit the configuration.");
        // load game
        this.game = new Game(this, text, sb);
        sb.setGame(game);
    }

    @Override
    public void onDisable() {
        if (this.game.isRunning()) {
            this.game.stop();
        }
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // TODO Handle all this in another file (it takes too much space)
        YamlConfiguration config = (YamlConfiguration) getConfig();
        String configVersion = config.getString("config-version", "1.2");
        if (configVersion.equals("1.6")) return;
        File configFile = new File(getDataFolder(), "config.yml");
        if (configVersion.equals("1.2")) { // Update config to 1.3
            try {
                FileWriter writer = new FileWriter(configFile, true);
                writer.append("""
                        # Game duration
                        game-duration:
                          hours: 2
                          minutes: 0""");
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> items = config.getStringList("items");
            items.addAll(List.of("netherite_upgrade_smithing_template", "cherry_sapling", "bamboo_planks", "SUSPICIOUS_SAND", "BRUSH"));
            config.set("items", items);
            configVersion = "1.3";
            config.set("config-version", configVersion);
            saveConfig();
        }
        if (configVersion.equals("1.3")) { // Update config to 1.6
            List<String> items = config.getStringList("items");
            items.addAll(List.of("MACE", "WIND_CHARGE"));
            config.set("items", items);
            configVersion = "1.6";
            config.set("config-version", configVersion);
            saveConfig();
        }
        File scenariosFolder = new File(this.getDataFolder(), "scenarios");
        if (!scenariosFolder.exists()) scenariosFolder.mkdirs();
        if (configVersion.equals("1.6")) { // Update config to 2.0 (scenarios and rarities update)
            File oldScenarioFile = new File(scenariosFolder, "old.yml");
            YamlConfiguration oldScenario = YamlConfiguration.loadConfiguration(oldScenarioFile);
            oldScenario.set("name", "Old Configuration");
            oldScenario.set("type", "list");
            oldScenario.set("stacks", config.getInt("stacks"));
            oldScenario.set("drop-interval", config.getInt("drop-interval"));
            oldScenario.set("drop-count", config.getInt("drop-count"));
            oldScenario.set("items", config.getStringList("items"));
            try {
                oldScenario.save(oldScenarioFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            config.set("default-scenario", "old");
            configVersion = "2.0";
            config.set("config-version", configVersion);
            saveConfig();
        }
        saveDefaultScenarios();
    }

    private void saveDefaultScenarios() {
        final List<String> scenarios = List.of("allitems", "base", "noweapon", "cheat", "rarities1");
        scenarios.forEach(scenario -> {
            String subPath = "scenarios/" + scenario + ".yml";
            File scenarioFile = new File(this.getDataFolder(), subPath);
            if (!scenarioFile.exists()) {
                InputStream link = this.getResource(subPath);
                assert link != null;
                try {
                    Files.copy(link, scenarioFile.getAbsoluteFile().toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /*
    2.0
    Scenarios -> Update plugin description accordingly
    The previous config will be transferred into a scenario, the 'itemChooseMode', 'stacks', 'drop-interval',
     'drop-count' and 'items' entries can be safely deleted
    Internal optimisations
    Stopped announcing advancements
    Fixed 2 bugs
    Ric command alias
    Changed permission to randomitemchallenge.admin
    New command logic
    default cheat scenario
    added rarities
    added default scenario Rarities I
     */

}
