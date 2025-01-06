package dev.tr25.worldfall;

import dev.tr25.worldfall.commands.MainWF;
import dev.tr25.worldfall.events.PlayerEvent;
import dev.tr25.worldfall.events.WFScoreboard;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main class for the WorldFall plugin.
 * This class is responsible for loading commands, events, and the config file.
 */
public final class WorldFall extends JavaPlugin {
    private String configPath;
    final PluginMeta pdf = getPluginMeta();
    public final String version = pdf.getVersion();
    public final String pluginName = pdf.getName();
    public final Component pluginPrefix = MiniMessage.miniMessage().deserialize("<obfuscated>[<reset><gold><bold>" + pluginName + "<reset><obfuscated>]<reset> ");
    private boolean wfActive = false;

    /**
     * Called when the plugin is enabled.
     * This method loads commands, events, and the config file.
     */
    @Override
    public void onEnable() {
        if (Bukkit.getLogger().isLoggable(java.util.logging.Level.INFO)) {
            // bStats connection
            int pluginID = 19126;
            new Metrics(this, pluginID);

            // Check if the server is running Paper
            if (!Bukkit.getName().equalsIgnoreCase("Paper")) {
                Bukkit.getLogger().warning(pluginName + " > This plugin is designed to run on Paper servers.");
                Bukkit.getLogger().warning(pluginName + " > Please consider using Paper for the best experience.");
            }

            // Plugin startup logic
            Bukkit.getLogger().info("\u001B[32m<-------------------------------------->\u001B[37m");
            Bukkit.getLogger().info("\u001B[33m" + pluginName + " \u001B[32m(version " + version + ")\u001B[37m");
            Bukkit.getLogger().info("\u001B[32mCreated by: " + pdf.getAuthors().get(0) + "\u001B[37m");
            Bukkit.getLogger().info("\u001B[32mWebsite: " + pdf.getWebsite() + "\u001B[37m");
            Bukkit.getLogger().info("\u001B[32m<-------------------------------------->\u001B[37m");
            PluginManager manager = Bukkit.getServer().getPluginManager();

            // Register commands and events
            Bukkit.getLogger().info(pluginName + " > Loading commands.");
            commandRegister();

            Bukkit.getLogger().info(pluginName + " > Loading events.");
            manager.registerEvents(new PlayerEvent(this), this);

            // Load config
            Bukkit.getLogger().info(pluginName + " > Loading config.");
            configRegister();

            // Load scoreboard
            WFScoreboard wfScoreboard = new WFScoreboard(this);
            String ticksString = getConfig().getString("scoreboard.ticks");
            if (ticksString == null) {
                Bukkit.getLogger().info(pluginName + " > Scoreboard ticks not found in config.yml. Using default value.");
                getConfig().set("scoreboard.ticks", 20);
                saveConfig();
                wfScoreboard.createScoreboard(20);
            } else {
                int ticks = Integer.parseInt(ticksString);
                wfScoreboard.createScoreboard(ticks);
            }

            // Check for updates
            Bukkit.getLogger().info(pluginName + " > Checking for updates...");
            try {
                UpdateCheck update = new UpdateCheck(this);
                if (update.isUpdateAvailable("paper")) {
                    Bukkit.getLogger().info(pluginName + " > An update is available in Modrinth!");
                } else Bukkit.getLogger().info(pluginName + " > No updates available. You are using the latest version.");
            } catch (Exception e) {
                Bukkit.getLogger().info(pluginName + " > An error occurred while checking for updates.");
                Bukkit.getLogger().info(pluginName + " > Please check your Internet connection or check manually in the following URL:");
                Bukkit.getLogger().info(pluginName + " > https://modrinth.com/plugin/WorldFall");
                Bukkit.getLogger().warning(pluginName + " > Error message: " + e.getMessage());
            }

            // Notify that the plugin has been loaded successfully
            Bukkit.getLogger().info(pluginName+" > Plugin loaded successfully!");
        } else {
            Bukkit.getLogger().warning(pluginName + " > This plugin requires INFO level logging to work properly.");
            Bukkit.getLogger().warning(pluginName + " > Please change the logging level in your server.properties file.");
        }
    }

    /**
     * Called when the plugin is disabled.
     * This method notifies the user that the plugin is shutting down.
     */
    @Override
    public void onDisable() {
        if (Bukkit.getLogger().isLoggable(java.util.logging.Level.INFO)) {
            // Plugin shutdown logic
            Bukkit.getLogger().info("<--------------------------------------------->");
            Bukkit.getLogger().info("Shutting down " + pluginName + " (version " + version + ")");
            Bukkit.getLogger().info("Thank you for using this plugin!");
            Bukkit.getLogger().info("<--------------------------------------------->");
        }
    }

    /**
     * Register commands for the plugin.
     */
    public void commandRegister() {
        this.getCommand("worldfall").setExecutor(new MainWF(this));
        this.getCommand("worldfall").setTabCompleter(new MainWF(this));
    }

    /**
     * Check if the WorldFall game has started.
     * @return true if the game has started, false otherwise.
     */
    public boolean isWfActive() {
        return wfActive;
    }

    /**
     * Set the status of the WorldFall game.
     * @param wfActive true if the game has started, false otherwise.
     */
    public void setWfActive(boolean wfActive) {
        this.wfActive = wfActive;
    }

    /**
     * Load the config file for the plugin.
     */
    public void configRegister() {
        File config = new File(this.getDataFolder(), "config.yml");
        configPath = config.getPath();
        if (!config.exists()) {
            if (Bukkit.getLogger().isLoggable(java.util.logging.Level.INFO)) Bukkit.getLogger().info(pluginName + " > config.yml not found. Generating new one!");
            this.getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    /**
     * Get the path to the config file.
     * @return Path to the config file.
     */
    public String getConfigPath() {
        return configPath;
    }
}
