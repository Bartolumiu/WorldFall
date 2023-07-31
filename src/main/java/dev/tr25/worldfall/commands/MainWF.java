package dev.tr25.worldfall.commands;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainWF implements CommandExecutor, TabCompleter {
    private final WorldFall wfr;
    
    public MainWF (WorldFall wfr) {
        this.wfr = wfr;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        /* Command sender is Console */
        if (!(commandSender instanceof Player)) {
            Bukkit.getLogger().info(wfr.pluginPrefix+"\u001B[31mCommand not available in Console!\u001B[37m");
            return false;
        } else {
            Player player = (Player) commandSender;
            Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();

            if (strings.length == 0) {
                player.sendMessage("§6---------------------------------", "§6§lWorldFall §aplugin by TR25", "§aVersion: §b"+wfr.version, "§aUse §b/worldfall cmd §ato see the command list", "§aAliases: §b/wf", "§6---------------------------------");
            } else {
                switch (strings[0]) {
                    case "cmd":
                        player.sendMessage("§6---------------------------------", "§aCommand List", "§6/worldfall§a: Main Command", "§6/worldfall cmd§a: Displays this list");
                        if (player.hasPermission("wf.config.reload")) player.sendMessage("§6/worldfall reload§a: Reloads the plugin config");
                        player.sendMessage("§6/worldfall version§a: Get the plugin version", "§6/worldfall status§a: Check WorldFall status");
                        if (player.hasPermission("wf.action.start")) player.sendMessage("§6/worldfall start§a: Starts WorldFall");
                        if (player.hasPermission("wf.action.stop")) player.sendMessage("§6/worldfall stop§a: Stops WorldFall");
                        if (player.hasPermission("wf.config.scoreboard.enable")) player.sendMessage("§6/worldfall scoreboard enable§a: Enables the sidebar");
                        if (player.hasPermission("wf.config.scoreboard.disable")) player.sendMessage("§6/worldfall scoreboard disable§a: Disables the sidebar");
                        player.sendMessage("§6---------------------------------");
                        break;
                    case "version":
                        player.sendMessage(wfr.pluginPrefix + "WorldFall for Paper (version " + wfr.version + ")");
                        break;
                    case "status":
                        player.sendMessage(wfr.pluginPrefix + "WorldFall Status: " + (wfr.wfActive ? "§a§lACTIVE" : "§c§lINACTIVE"));
                        break;
                    case "start":
                        if (player.hasPermission("wf.action.start")) {
                            if (wfr.wfStarted()) {
                                player.sendMessage(wfr.pluginPrefix + "§6WorldFall already started!");
                            } else {
                                for (Object onlinePlayer : onlinePlayers) {
                                    ((Player) onlinePlayer).setGameMode(GameMode.SURVIVAL);
                                }
                                wfr.wfActive = true;
                                player.sendMessage(wfr.pluginPrefix + "§aWorldFall started!");
                            }
                        } else {
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "stop":
                        if (player.hasPermission("wf.action.stop")) {
                            if (!(wfr.wfStarted())) {
                                player.sendMessage(wfr.pluginPrefix + "§4WorldFall not active. Use §b/worldfall start §4to start.");
                            } else {
                                for (Object onlinePlayer : onlinePlayers) {
                                    ((Player) onlinePlayer).setGameMode(GameMode.ADVENTURE);
                                }
                                wfr.wfActive = false;
                                player.sendMessage(wfr.pluginPrefix + "§aWorldFall stopped!");
                            }
                        } else {
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "reload":
                        if (player.hasPermission("wf.config.reload")) {
                            player.sendMessage(wfr.pluginPrefix + "Reloading WorldFall config");
                            wfr.reloadConfig();
                            player.sendMessage(wfr.pluginPrefix + "Config reloaded!");
                        } else {
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "scoreboard":
                        switch (strings[1]) {
                            case "enable":
                                if (player.hasPermission("wf.config.scoreboard.enable")) {
                                    if (wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                        player.sendMessage(wfr.pluginPrefix + "§6Scoreboard already active! Use §b/worldfall scoreboard disable §6to disable it or change the plugin config.");
                                    } else {
                                        wfr.getConfig().set("scoreboard.enabled", true);
                                        wfr.saveConfig();
                                        player.sendMessage(wfr.pluginPrefix + "§aScoreboard enabled!");
                                    }
                                } else {
                                    player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                                }
                                break;
                            case "disable":
                                if (player.hasPermission("wf.config.scoreboard.disable")) {
                                    if (!wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                        player.sendMessage(wfr.pluginPrefix + "§4Scoreboard not active. Use §b/worldfall scoreboard enable §4to enable it or change the plugin config.");
                                    } else {
                                        wfr.getConfig().set("scoreboard.enabled", false);
                                        wfr.saveConfig();
                                        player.sendMessage(wfr.pluginPrefix + "§aScoreboard disabled!");
                                    }
                                } else {
                                    player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                                }
                                break;
                            default:
                                player.sendMessage(wfr.pluginPrefix + "§4Wrong command. Use §6/worldfall cmd §4for command list");
                                break;
                        }
                        break;
                    default:
                        player.sendMessage(wfr.pluginPrefix + "§4Wrong command. Use §6/worldfall cmd §4for command list");
                        break;
                }
            }
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> subcommands;
        List<String> secondSubcommands;

        switch (strings.length) {
            case 1:
                subcommands = new ArrayList<>();
                subcommands.add("cmd");
                subcommands.add("version");
                subcommands.add("status");
                if (commandSender.hasPermission("wf.action.start")) subcommands.add("start");
                if (commandSender.hasPermission("wf.action.stop")) subcommands.add("stop");
                if (commandSender.hasPermission("wf.config.reload")) subcommands.add("reload");
                if (commandSender.hasPermission("wf.config.scoreboard.enable") || commandSender.hasPermission("wf.config.scoreboard.disable")) subcommands.add("scoreboard");
                return subcommands;
            case 2:
                secondSubcommands = new ArrayList<>();
                if (commandSender.hasPermission("wf.config.scoreboard.enable")) secondSubcommands.add("enable");
                if (commandSender.hasPermission("wf.config.scoreboard.disable")) secondSubcommands.add("disable");
                return secondSubcommands;
            default:
                return null;
        }
    }
}
