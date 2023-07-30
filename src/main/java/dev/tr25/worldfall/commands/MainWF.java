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

    public MainWF(WorldFall wfr) {
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

            /* Command has arguments */
            if (strings.length > 0) {
                if (strings[0].equalsIgnoreCase("cmd")) {
                    player.sendPlainMessage("§6---------------------------------");
                    player.sendPlainMessage("§aCommand List");
                    player.sendPlainMessage("§6/worldfall§a: Main Command");
                    player.sendPlainMessage("§6/worldfall cmd§a: Displays this list");
                    if (player.hasPermission("wf.config.reload")) player.sendPlainMessage("§6/worldfall reload§a: Reloads the plugin config");
                    player.sendPlainMessage("§6/worldfall version§a: Get the plugin version");
                    player.sendPlainMessage("§6/worldfall status§a: Check WorldFall status");
                    if (player.hasPermission("wf.action.start")) player.sendPlainMessage("§6/worldfall start§a: Starts WorldFall");
                    if (player.hasPermission("wf.action.stop")) player.sendPlainMessage("§6/worldfall stop§a: Stops WorldFall");
                    if (player.hasPermission("wf.config.scoreboard.enable")) player.sendPlainMessage("§6/worldfall scoreboard enable§a: Enables the sidebar");
                    if (player.hasPermission("wf.config.scoreboard.disable")) player.sendPlainMessage("§6/worldfall scoreboard disable§a: Disables the sidebar");
                    player.sendPlainMessage("§6---------------------------------");
                } else if (strings[0].equalsIgnoreCase("version")) {
                    player.sendPlainMessage(wfr.pluginPrefix + "WorldFall for Paper (version " + wfr.version + ")");
                } else if (strings[0].equalsIgnoreCase("status")) {
                    player.sendPlainMessage(wfr.pluginPrefix + "WorldFall Status: §6" + (wfr.wfActive ? "ACTIVE" : "INACTIVE"));
                } else if (strings[0].equalsIgnoreCase("start")) {
                    if (player.hasPermission("wf.action.start")) {
                        if (wfr.wfStarted()) {
                            player.sendPlainMessage(wfr.pluginPrefix + "§6WorldFall already started!");
                        } else {
                            for (Object eachplayer : onlinePlayers) {
                                ((Player) eachplayer).setGameMode(GameMode.SURVIVAL);
                            }
                            wfr.wfActive = true;
                            player.sendPlainMessage(wfr.pluginPrefix + "§aWorldFall started!");
                        }
                    } else {
                        player.sendPlainMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                    }
                } else if (strings[0].equalsIgnoreCase("stop")) {
                    if (player.hasPermission("wf.action.stop")) {
                        if (!(wfr.wfStarted())) {
                            player.sendPlainMessage(wfr.pluginPrefix + "§4WorldFall not active. Use §b/worldfall start §4to start.");
                        } else {
                            for (Object onlinePlayer : onlinePlayers) {
                                ((Player) onlinePlayer).setGameMode(GameMode.ADVENTURE);
                            }
                            wfr.wfActive = false;
                            player.sendPlainMessage(wfr.pluginPrefix + "§aWorldFall stopped!");
                        }
                    } else {
                        player.sendPlainMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                    }
                } else if (strings[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("wf.config.reload")) {
                        player.sendPlainMessage(wfr.pluginPrefix + "Reloading WorldFall config");
                        wfr.reloadConfig();
                        player.sendPlainMessage(wfr.pluginPrefix + "Config reloaded!");
                    } else {
                        player.sendPlainMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                    }
                } else if (strings[0].equalsIgnoreCase("scoreboard")) {
                    if (strings[1].equalsIgnoreCase("enable")) {
                        if (player.hasPermission("wf.config.scoreboard.enable")) {
                            if (wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                player.sendPlainMessage(wfr.pluginPrefix + "§6Scoreboard already active! Use §b/worldfall scoreboard disable §6to disable it or change the plugin config.");
                            } else {
                                wfr.getConfig().set("scoreboard.enabled", true);
                                wfr.saveConfig();
                                player.sendPlainMessage(wfr.pluginPrefix + "§aScoreboard enabled!");
                            }
                        } else {
                            player.sendPlainMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                    } else if (strings[1].equalsIgnoreCase("disable")) {
                        if (player.hasPermission("wf.config.scoreboard.disable")) {
                            if (!wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                player.sendPlainMessage(wfr.pluginPrefix + "§4Scoreboard not active. Use §b/worldfall scoreboard enable §4to enable it or change the plugin config.");
                            } else {
                                wfr.getConfig().set("scoreboard.enabled", false);
                                wfr.saveConfig();
                                player.sendPlainMessage(wfr.pluginPrefix + "§aScoreboard disabled!");
                            }
                        } else {
                            player.sendPlainMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                    } else {
                        player.sendPlainMessage(wfr.pluginPrefix + "§4Wrong command. Use §6/worldfall cmd §4for command list");
                    }
                } else if (strings[0].equalsIgnoreCase("test")) {
                    player.sendPlainMessage(player.getEffectivePermissions().toString());
                } else {
                        player.sendPlainMessage(wfr.pluginPrefix+"§4Wrong command. Use §6/worldfall cmd §4for command list");
                }
            } else {
                player.sendPlainMessage("§6---------------------------------");
                player.sendPlainMessage("§6§lWorldFall §aplugin by TR25");
                player.sendPlainMessage("§aVersion: §b"+wfr.version);
                player.sendPlainMessage("§aUse §b/worldfall cmd §ato see the command list");
                player.sendPlainMessage("§aAliases: §b/wf");
                player.sendPlainMessage("§6---------------------------------");
            }
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> subcommands = null;
        List<String> secondSubcommands = null;

        if (strings.length == 1) {
            subcommands = new ArrayList<>();
            subcommands.add("cmd");
            subcommands.add("version");
            subcommands.add("status");
            if (commandSender.hasPermission("wf.action.start")) subcommands.add("start");
            if (commandSender.hasPermission("wf.action.stop")) subcommands.add("stop");
            if (commandSender.hasPermission("wf.config.reload")) subcommands.add("reload");
            if (commandSender.hasPermission("wf.config.scoreboard.enable") || commandSender.hasPermission("wf.config.scoreboard.disable")) subcommands.add("scoreboard");
        } else if (strings.length == 2) {
            secondSubcommands = new ArrayList<>();
            if (commandSender.hasPermission("wf.config.scoreboard.enable")) secondSubcommands.add("enable");
            if (commandSender.hasPermission("wf.config.scoreboard.disable")) secondSubcommands.add("disable");
        }

        switch (strings.length) {
            case 1:
                return subcommands;
            case 2:
                return secondSubcommands;
            default:
                return null;
        }
    }
}
