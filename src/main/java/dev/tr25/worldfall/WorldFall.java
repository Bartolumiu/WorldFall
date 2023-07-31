package dev.tr25.worldfall;

import dev.tr25.worldfall.commands.MainWF;
import dev.tr25.worldfall.events.PlayerEvent;
import dev.tr25.worldfall.events.WFScoreboard;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
public final class WorldFall extends JavaPlugin {
    public String configPath;
    final PluginDescriptionFile pdf = getDescription();
    public final String version = pdf.getVersion();
    public final String pluginName = pdf.getName();
    public final String pluginPrefix = ChatColor.translateAlternateColorCodes('&', "&k[&r&6&l"+pluginName+"&f&k]&r ");
    public boolean wfActive = false;

    @Override
    public void onEnable() {
        // bStats connection
        int pluginId = 19126;
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        Bukkit.getLogger().info("\u001B[32m<-------------------------------------->\u001B[37m");
        Bukkit.getLogger().info("\u001B[33m"+pluginName+" \u001B[32m(version "+version+")\u001B[37m");
        Bukkit.getLogger().info("\u001B[32mCreated by: "+pdf.getAuthors().get(0)+"\u001B[37m");
        Bukkit.getLogger().info("\u001B[32mWebsite: "+pdf.getWebsite()+"\u001B[37m");
        Bukkit.getLogger().info("\u001B[32m<-------------------------------------->\u001B[37m");
        PluginManager manager = Bukkit.getServer().getPluginManager();

        Bukkit.getLogger().info(pluginPrefix+"Loading commands.");
        commandRegister();

        Bukkit.getLogger().info(pluginPrefix+"Loading events.");
        manager.registerEvents(new PlayerEvent(this), this);

        Bukkit.getLogger().info(pluginPrefix+"Loading config.");
        configRegister();

        WFScoreboard wfScoreboard = new WFScoreboard(this);
        wfScoreboard.createScoreboard(Integer.parseInt(getConfig().getString("scoreboard.ticks")));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("<--------------------------------------------->");
        Bukkit.getLogger().info("Shutting down "+pluginName+" (version "+version+")");
        Bukkit.getLogger().info("Thank you for using this plugin!");
        Bukkit.getLogger().info("<--------------------------------------------->");
    }

    public void commandRegister() {
        this.getCommand("worldfall").setExecutor(new MainWF(this));
        this.getCommand("worldfall").setTabCompleter(new MainWF(this));
    }

    public boolean wfStarted() {
        return wfActive;
    }

    public void configRegister() {
        File config = new File(this.getDataFolder(), "config.yml");
        configPath = config.getPath();
        if (!config.exists()) {
            Bukkit.getLogger().info(pluginPrefix+"config.yml not found. Generating new one!");
            this.getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }
}
