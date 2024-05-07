package dev.tr25.worldfall.commands;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main command class for the WorldFall plugin.
 * This class is responsible for handling the main command and its subcommands.
 */
public class MainWF implements CommandExecutor, TabCompleter {
    private final WorldFall wfr;

    /**
     * Constructor
     * @param wfr WorldFall instance
     */
    public MainWF(WorldFall wfr) {
        this.wfr = wfr;
    }

    /**
     * Handle the main command and its subcommands.
     * @param commandSender Command sender
     * @param command Command
     * @param s Command label
     * @param strings Command arguments
     * @return true if the command was executed successfully, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        /* Command sender is Console */
        if (!(commandSender instanceof Player)) {
            Bukkit.getLogger().info(wfr.pluginName+"\u001B[31mCommand not available in Console!\u001B[37m");
            return false;
        } else {
            Player player = (Player) commandSender;
            Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();

            if (strings.length == 0) {
                // Display plugin information if no arguments are provided
                player.sendMessage("§6---------------------------------", "§6§lWorldFall §aplugin by TR25", "§aVersion: §b"+wfr.version, "§aUse §b/worldfall cmd §ato see the command list", "§aAliases: §b/wf", "§6---------------------------------");
            } else {
                // Handle subcommands
                switch (strings[0]) {
                    case "cmd":
                        // Display command list
                        // Check for permissions before displaying certain commands
                        player.sendMessage("§6---------------------------------", "§aCommand List", "§6/worldfall§a: Main Command", "§6/worldfall cmd§a: Displays this list");
                        if (player.hasPermission("wf.config.reload")) player.sendMessage("§6/worldfall reload§a: Reloads the plugin config");
                        player.sendMessage("§6/worldfall version§a: Get the plugin version", "§6/worldfall status§a: Check WorldFall status");
                        if (player.hasPermission("wf.action.start")) player.sendMessage("§6/worldfall start§a: Starts WorldFall in 5 seconds (or specified time)");
                        if (player.hasPermission("wf.action.stop")) player.sendMessage("§6/worldfall stop§a: Stops WorldFall");
                        if (player.hasPermission("wf.config.scoreboard.enable")) player.sendMessage("§6/worldfall scoreboard enable§a: Enables the sidebar");
                        if (player.hasPermission("wf.config.scoreboard.disable")) player.sendMessage("§6/worldfall scoreboard disable§a: Disables the sidebar");
                        if (player.hasPermission("wf.action.tp")) player.sendMessage("§6/worldfall tp§a: Teleport all players to you");
                        player.sendMessage("§6---------------------------------");
                        break;
                    case "version":
                        // Display plugin version
                        player.sendMessage(wfr.pluginPrefix + "WorldFall for Paper (version " + wfr.version + ")");
                        break;
                    case "status":
                        // Display WorldFall status
                        player.sendMessage(wfr.pluginPrefix + "WorldFall Status: " + (wfr.wfActive ? "§a§lACTIVE" : "§c§lINACTIVE"));
                        break;
                    case "start":
                        // Start WorldFall if the player uses /worldfall start
                        // Check for permissions before starting the game
                        if (player.hasPermission("wf.action.start")) {
                            // Check if WorldFall is already active
                            if (wfr.wfStarted()) {
                                // Display message if WorldFall is already active
                                player.sendMessage(wfr.pluginPrefix + "§6WorldFall already started!");
                            } else {
                                // Set default time to 5 seconds
                                int time = 5;
                                // Check if a time argument is provided
                                if (strings.length > 1) {
                                    time = Integer.parseInt(strings[1]);
                                    // Check for negative time values
                                    if (time < 0) {
                                        player.sendMessage(wfr.pluginPrefix + "§4Time cannot be negative!");
                                        return true;
                                    }

                                    // Announce the start of WorldFall
                                    Bukkit.broadcastMessage(wfr.pluginPrefix + "§6WorldFall starting in §b" + time + " seconds");

                                    // Wait for the specified time
                                    try {
                                        Thread.sleep(time * 1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();

                                    }
                                } else Bukkit.broadcastMessage(wfr.pluginPrefix + "§6WorldFall starting in §b" + time + " seconds");
                                Bukkit.getScheduler().runTaskLater(wfr, () -> {
                                    for (Object onlinePlayer : onlinePlayers) {
                                        // Set all players to survival mode if they are on adventure mode
                                        if (((Player) onlinePlayer).getGameMode().equals(GameMode.ADVENTURE)) {
                                            ((Player) onlinePlayer).setGameMode(GameMode.SURVIVAL);
                                        }
                                    }
                                    // Set WorldFall to active and display message
                                    wfr.wfActive = true;
                                    Bukkit.broadcastMessage(wfr.pluginPrefix + "§aWorldFall started!");
                                }, time * 20L);
                            }
                        } else {
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "stop":
                        // Stop WorldFall if the player uses /worldfall stop
                        // Check for permissions before stopping the game
                        if (player.hasPermission("wf.action.stop")) {
                            // Check if WorldFall is active
                            if (!(wfr.wfStarted())) {
                                // Display message if WorldFall is not active
                                player.sendMessage(wfr.pluginPrefix + "§4WorldFall not active. Use §b/worldfall start §4to start.");
                            } else {
                                // Set all players to adventure mode and stop WorldFall
                                for (Object onlinePlayer : onlinePlayers) {
                                    ((Player) onlinePlayer).setGameMode(GameMode.ADVENTURE);
                                }
                                // Set WorldFall to inactive and display message
                                wfr.wfActive = false;
                                Bukkit.broadcastMessage(wfr.pluginPrefix + "§aWorldFall stopped! You may now relax (for now)");
                            }
                        } else {
                            // Display message if the player does not have permission to stop WorldFall
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "reload":
                        // Reload the plugin config if the player uses /worldfall reload
                        // Check for permissions before reloading the config
                        if (player.hasPermission("wf.config.reload")) {
                            // Reload the plugin config and display message
                            player.sendMessage(wfr.pluginPrefix + "Reloading WorldFall config");
                            wfr.reloadConfig();
                            player.sendMessage(wfr.pluginPrefix + "Config reloaded!");
                        } else {
                            // Display message if the player does not have permission to reload the config
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    case "scoreboard":
                        // Enable or disable the scoreboard if the player uses /worldfall scoreboard
                        switch (strings[1]) {
                            case "enable":
                                // Enable the scoreboard if the player uses /worldfall scoreboard enable
                                // Check for permissions before enabling the scoreboard
                                if (player.hasPermission("wf.config.scoreboard.enable")) {
                                    // Check if the scoreboard is already enabled
                                    if (wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                        // Display message if the scoreboard is already enabled
                                        player.sendMessage(wfr.pluginPrefix + "§6Scoreboard already active! Use §b/worldfall scoreboard disable §6to disable it or change the plugin config.");
                                    } else {
                                        // Enable the scoreboard and display message
                                        wfr.getConfig().set("scoreboard.enabled", true);
                                        wfr.saveConfig();
                                        player.sendMessage(wfr.pluginPrefix + "§aScoreboard enabled!");
                                    }
                                } else {
                                    // Display message if the player does not have permission to enable the scoreboard
                                    player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                                }
                                break;
                            case "disable":
                                // Disable the scoreboard if the player uses /worldfall scoreboard disable
                                // Check for permissions before disabling the scoreboard
                                if (player.hasPermission("wf.config.scoreboard.disable")) {
                                    // Check if the scoreboard is already disabled
                                    if (!wfr.getConfig().getBoolean("scoreboard.enabled")) {
                                        // Display message if the scoreboard is already disabled
                                        player.sendMessage(wfr.pluginPrefix + "§4Scoreboard not active. Use §b/worldfall scoreboard enable §4to enable it or change the plugin config.");
                                    } else {
                                        // Disable the scoreboard and display message
                                        wfr.getConfig().set("scoreboard.enabled", false);
                                        wfr.saveConfig();
                                        player.sendMessage(wfr.pluginPrefix + "§aScoreboard disabled!");
                                    }
                                } else {
                                    // Display message if the player does not have permission to disable the scoreboard
                                    player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                                }
                                break;
                            default:
                                // Display message if the player uses an invalid subcommand
                                player.sendMessage(wfr.pluginPrefix + "§4Wrong command. Use §6/worldfall cmd §4for command list");
                                break;
                        }
                        break;
                    case "tp":
                        if (player.hasPermission("wf.action.tp")) {
                            // Check if the command is /worldfall tp confirm
                            if (strings.length > 1 && strings[1].equals("confirm")) {
                                // Announce teleportation to the server
                                Bukkit.broadcastMessage(wfr.pluginPrefix + "§6Teleporting all players to §b" + player.getName());
                                for (Object onlinePlayer : onlinePlayers) {
                                    Bukkit.getLogger().info(wfr.pluginPrefix + " > Teleporting " + ((Player) onlinePlayer).getName() + " to " + player.getName());
                                    Player otherPlayer = (Player) onlinePlayer;
                                    // Only execute if player is in survival mode
                                    if ((!wfr.wfStarted() && otherPlayer.getGameMode() != GameMode.ADVENTURE) || (wfr.wfStarted() && otherPlayer.getGameMode() != GameMode.SURVIVAL)) continue;
                                    // Notify the player that they are being teleported
                                    otherPlayer.sendMessage(wfr.pluginPrefix + "§6Teleporting to §b" + player.getName());
                                    otherPlayer.sendMessage(wfr.pluginPrefix + "§6Please wait...");
                                    // Apply slowness & blindness effects to the player
                                    otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000, 255));
                                    otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000, 255));
                                    // Prevent player from moving or jumping while teleporting
                                    otherPlayer.setWalkSpeed(0);
                                    otherPlayer.setGravity(false);
                                    otherPlayer.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY()+0.01f, player.getLocation().getZ()));
                                    // Teleport the player to the invoker
                                    otherPlayer.teleport(player);
                                    // Reset gravity
                                    otherPlayer.setGravity(true);
                                    // Remove potion effects
                                    otherPlayer.removePotionEffect(PotionEffectType.SLOW);
                                    otherPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
                                    // Reset walk speed
                                    otherPlayer.setWalkSpeed(0.2f);
                                    // Notify the player
                                    otherPlayer.sendMessage(wfr.pluginPrefix + "§aTeleport successful!");
                                }
                            } else {
                                // Ask for confirmation before teleporting
                                player.sendMessage(wfr.pluginPrefix + "§6Are you sure you want to teleport all players to you?");
                                player.sendMessage(wfr.pluginPrefix + "§6Type §b/worldfall tp confirm §6to confirm.");
                            }
                        } else {
                            // Display message if the player does not have permission to teleport players
                            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
                        }
                        break;
                    default:
                        // Display message if the player uses an invalid subcommand
                        player.sendMessage(wfr.pluginPrefix + "§4Wrong command. Use §6/worldfall cmd §4for command list");
                        break;
                }
            }
            return true;
        }
    }

    /**
     * Handle tab completion for the main command and its subcommands.
     * @param commandSender Command sender
     * @param command Command
     * @param s Command label
     * @param strings Command arguments
     * @return List of possible tab completions
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> subcommands;
        List<String> secondSubcommands;

        // Check the number of arguments provided
        switch (strings.length) {
            case 1:
                // Provide subcommands for the main command
                // Check for permissions before displaying certain subcommands
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
                // Provide subcommands for the scoreboard command
                // Check for permissions before displaying certain subcommands
                secondSubcommands = new ArrayList<>();
                if (commandSender.hasPermission("wf.config.scoreboard.enable")) secondSubcommands.add("enable");
                if (commandSender.hasPermission("wf.config.scoreboard.disable")) secondSubcommands.add("disable");
                return secondSubcommands;
            default:
                // Just return null if the number of arguments is invalid (more than 2)
                return null;
        }
    }
}