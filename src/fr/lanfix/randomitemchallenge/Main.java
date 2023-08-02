package fr.lanfix.randomitemchallenge;

import fr.lanfix.randomitemchallenge.commands.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.events.GameEvents;
import fr.lanfix.randomitemchallenge.events.ItemEvents;
import fr.lanfix.randomitemchallenge.game.Game;
import fr.lanfix.randomitemchallenge.world.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private Game game;

    @Override
    public void onEnable() {
        WorldManager.createWorldManager(getConfig().getStringList("biomes-blacklist"), getConfig().getInt("border", 500));
        this.game = new Game(this);
        // save default config
        this.saveDefaultConfig();
        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GameEvents(this.game), this);
        pluginManager.registerEvents(new ItemEvents(), this);
        // Register commands
        getCommand("randomitemchallenge").setExecutor(new RandomItemChallenge(this.game));
    }

    @Override
    public void onDisable() {
        if (this.game.isRunning()) {
            this.game.stop();
        }
    }

    /*
    Fixed another bug with the scoreboard
    Fixed a potential bug with hunger
    Fixed a bug at the end of a game
    Did many internal optimisations
    The compass is now on the last slot of the hotbar instead of the first
    Game now runs on a separate world
    Disabled dimensions (would be a mess to control, and it does not fit the gameplay)
    Implemented Chunky for a small chunk pre-generation
     */

}