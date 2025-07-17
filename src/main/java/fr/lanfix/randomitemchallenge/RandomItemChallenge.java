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
                getConfig().getBoolean("use-separate-world", true));
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
        if (configVersion.startsWith("1.")) {
            Bukkit.getLogger().warning("[Random Item Challenge] Found old configuration file of older version (<2.0).");
            Bukkit.getLogger().warning("Please delete the RandomItemChallenge folder if you want to play with the new version (it introduces many breaking changes).");
            Bukkit.getLogger().info("You can make a backup of your old config if you liked it, in order to put your options into your own scenario");
        }
        File configFile = new File(getDataFolder(), "config.yml");
        // Update config (only after 2.0)
        File scenariosFolder = new File(this.getDataFolder(), "scenarios");
        if (!scenariosFolder.exists()) scenariosFolder.mkdirs();
        saveDefaultScenarios();
    }

    private void saveDefaultScenarios() {
        final List<String> scenarios = List.of("allitems", "base", "noweapon", "cheat", "rarities1", "rarities2");
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
    Added rarities2 scenario (new default scenario)
    Added :
        Vault and trial keys (1.21)
        Resin Clump and Pale moss (1.21.4)
        Happy Ghast spawn egg and Purple harness (1.21.6)
    Moved wind charges to legendary
     */

}
