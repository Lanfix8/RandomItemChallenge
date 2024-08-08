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
        pluginManager.registerEvents(new GameEvents(this.game), this);
        pluginManager.registerEvents(new ItemEvents(game), this);
        // Register commands
        getCommand("randomitemchallenge").setExecutor(new RandomItemChallengeCommand(this.game, this.text));
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
        // load game
        this.game = new Game(this, text, sb,
                Scenario.loadScenario(this, getConfig().getString("default-scenario")));
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
        // TODO Update old config to create a new scenario with old settings named old which will be the default-scenario for them.
        // TODO Save default scenarios
    }

    /*
    2.0
    Scenarios -> Update plugin description accordingly
    Internal optimisations
    Stopped announcing advancements
     */

}
