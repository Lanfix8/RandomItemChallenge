package fr.lanfix.randomitemchallenge;

import fr.lanfix.randomitemchallenge.commands.RandomItemChallenge;
import fr.lanfix.randomitemchallenge.events.GameEvents;
import fr.lanfix.randomitemchallenge.events.ItemEvents;
import fr.lanfix.randomitemchallenge.game.GameManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        this.gameManager = new GameManager(this);
        // save default config
        this.saveDefaultConfig();
        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GameEvents(this.gameManager), this);
        pluginManager.registerEvents(new ItemEvents(), this);
        // Register commands
        getCommand("randomitemchallenge").setExecutor(new RandomItemChallenge(this.gameManager));
    }

    @Override
    public void onDisable() {
        this.gameManager.stopAllGames();
    }

    /*
    Fixed another bug with the scoreboard
     */

}