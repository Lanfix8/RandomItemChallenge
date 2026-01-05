package fr.lanfix.randomitemchallenge;

import fr.lanfix.randomitemchallenge.commands.RandomItemChallengeCommand;
import fr.lanfix.randomitemchallenge.events.GameEvents;
import fr.lanfix.randomitemchallenge.events.ItemEvents;
import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.game.scenario.Configuration;
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
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

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
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RandomItemChallengeExpansion(this.game).register();
        }
    }

    private void loadTexts() {
        File textsFile = new File(this.getDataFolder(), "texts.yml");
        this.text = new Text(YamlConfiguration.loadConfiguration(textsFile));
    }

    private void loadGame() {
        // load scoreboard
        File scoreboardFile = new File(this.getDataFolder(), "scoreboard.yml");
        YamlConfiguration sbConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        boolean customScoreboard = sbConfig.getBoolean("custom-scoreboard");
        boolean PAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        ScoreboardManager sb = customScoreboard ?
                new ScoreboardManager(game, sbConfig.getStringList("scoreboard"), PAPIEnabled) : new NoScoreboard();
        // load worldmanager
        WorldManager.createWorldManager(getConfig().getStringList("biomes-blacklist"),
                getConfig().getInt("border", 500),
                getConfig().getBoolean("use-separate-world", true));
        // load scenarios
        Configuration.loadScenarios(this);
        if (Configuration.defaultScenario == null) Bukkit.getLogger().warning("[RandomItemChallenge] Unknown default scenario, please edit the configuration.");
        // load game
        this.game = new Game(this, text, sb);
        sb.setGame(game);
    }

    public void reload() {
        reloadConfig();
        loadTexts();
        loadGame();
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
        if (configVersion.startsWith("1.")) {
            Bukkit.getLogger().warning("[Random Item Challenge] Found old Configuration file of older version (<2.0).");
            Bukkit.getLogger().warning("Please delete the RandomItemChallenge folder if you want to play with the new version (it introduces many breaking changes).");
            Bukkit.getLogger().info("You can make a backup of your old config if you liked it, in order to put your options into your own scenario");
        }
        // Update config (only after 2.0)
        if (configVersion.startsWith("2.0")) {
            Bukkit.getLogger().warning("If you want to benefit from recent changes in default scenarios, please remove them from the plugin's folder so the new ones can be extracted");
            Bukkit.getLogger().warning("Beware that in your old config, the 'game-duration' section is no longer a thing. There is no time limit in the game anymore");
            configVersion = "2.2";
        }
        config.set("config-version", configVersion);
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Save default resources
        saveDefaultResources();
    }

    private void saveDefaultResources() {
        saveResource("scoreboard.yml", false);
        saveResource("texts.yml", false);
        File scenariosFolder = new File(this.getDataFolder(), "scenarios");
        if (!scenariosFolder.exists()) scenariosFolder.mkdirs();
        String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        try (JarFile jar = new JarFile(jarPath)) {
            jar.stream().forEach(jarEntry -> {
                String name = jarEntry.getName();
                // We only save scenarios and custom drops
                if (name.startsWith("scenarios/") || name.startsWith("custom_drops/")) {
                    File outFile = new File(this.getDataFolder(), name);
                    if (!outFile.exists()) {
                        saveResource(name, false);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    New drops
    - Stone spear (Epic)
    - Ultimate Spear
    - Godly Spear
    Updated parts of the code for 1.21.11
    There is no time limit anymore
    Game now support drop periods in seconds
    Changed default custom scoreboard
    Fixed typo in texts
    Reload subcommand
    Added Scenario to PAPI
    Added Scenario line in scoreboard
     */

}
