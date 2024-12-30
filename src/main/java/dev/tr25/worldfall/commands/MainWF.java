package dev.tr25.worldfall.commands;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
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
    private BukkitTask startTask;

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
     * @param args Command arguments
     * @return true if the command was executed successfully, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        /* Command sender is Console */
        if (!(commandSender instanceof Player)) {
            Bukkit.getLogger().info(wfr.pluginName+"\u001B[31mCommand not available in Console!\u001B[37m");
            return false;
        }

        Player player = (Player) commandSender;

        if (args.length == 0) {
            displayInfo(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "cmd" -> displayCommandList(player);
            case "version" -> displayVersion(player);
            case "status" -> displayStatus(player);
            case "start" -> startWorldFall(player, args);
            case "stop" -> stopWorldFall(player);
            case "reload" -> reloadConfig(player);
            case "scoreboard" -> handleScoreboard(player, args);
            case "tp" -> teleportPlayers(player, args);
            default -> player.sendMessage(wfr.pluginPrefix + "§4Unknown command. Use §6/worldfall cmd §4for a list of commands");
        }

        return true;
    }

    /**
     * Displays information about the WorldFall plugin to the specified player.
     *
     * @param player the player to whom the information will be displayed
     */
    private void displayInfo(Player player) {
        player.sendMessage(
                "§6---------------------------------",
                "§6§lWorldFall §aplugin by TR25",
                "§aVersion: §b"+wfr.version,
                "§aUse §b/worldfall cmd §ato see the command list",
                "§aAliases: §b/wf",
                "§6---------------------------------"
        );
    }

    /**
     * Displays a list of available commands to the specified player.
     * The list includes commands that the player has permission to use.
     *
     * @param player the player to whom the command list will be displayed
     */
    private void displayCommandList(Player player) {
        player.sendMessage(
                "§6---------------------------------",
                "§aCommand List",
                "§6/worldfall§a: Main Command",
                "§6/worldfall cmd§a: Displays this list",
                "§6/worldfall version§a: Get the plugin version",
                "§6/worldfall status§a: Check WorldFall status"
        );
        if (player.hasPermission("wf.config.reload")) player.sendMessage("§6/worldfall reload§a: Reloads the plugin config");
        if (player.hasPermission("wf.action.start")) player.sendMessage("§6/worldfall start§a: Starts WorldFall in 5 seconds (or specified time)");
        if (player.hasPermission("wf.action.stop")) player.sendMessage("§6/worldfall stop§a: Stops WorldFall");
        if (player.hasPermission("wf.config.scoreboard.enable")) player.sendMessage("§6/worldfall scoreboard enable§a: Enables the sidebar");
        if (player.hasPermission("wf.config.scoreboard.disable")) player.sendMessage("§6/worldfall scoreboard disable§a: Disables the sidebar");
        if (player.hasPermission("wf.action.tp")) player.sendMessage("§6/worldfall tp§a: Teleport all players to you");
        player.sendMessage("§6---------------------------------");
    }

    /**
     * Sends a message to the specified player displaying the current version of the WorldFall plugin.
     *
     * @param player the player to whom the version message will be sent
     */
    private void displayVersion(Player player) {
        player.sendMessage(wfr.pluginPrefix + "WorldFall for Paper (version " + wfr.version + ")");
    }

    /**
     * Displays the current status of the WorldFall plugin to the specified player.
     *
     * @param player The player to whom the status message will be sent.
     */
    private void displayStatus(Player player) {
        player.sendMessage(wfr.pluginPrefix + "WorldFall Status: " + (wfr.wfActive ? "§a§lACTIVE" : "§c§lINACTIVE"));
    }

    /**
     * Starts the WorldFall event for the specified player.
     *
     * * This method performs the following actions:
     * - Checks if the player has the required permission to start the event.
     * - Sends a message to the player if they lack the required permission.
     * - Checks if the WorldFall event has already started and notifies the player if it has.
     * - Parses the countdown time from the command arguments, defaulting to 5 seconds if not provided.
     * - Validates the countdown time to ensure it is not negative.
     * - Cancels any previously scheduled start task.
     * - Broadcasts a message to all players indicating the countdown to the start of the WorldFall event.
     * - Schedules a task to start the WorldFall event after the specified countdown time and saves the task reference.
     * - Changes the game mode of all players in Adventure mode to Survival mode when the event starts.
     * - Sets the WorldFall active flag to true.
     * - Broadcasts a message to all players indicating that the WorldFall event has started.
     * 
     * @param player The player who initiated the command.
     * @param args   The command arguments. The second argument (if present) specifies the countdown time in seconds.
     */
    private void startWorldFall(Player player, String[] args) {
        if (!player.hasPermission("wf.action.start")) {
            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
            return;
        }

        if (wfr.wfStarted()) {
            player.sendMessage(wfr.pluginPrefix + "§6WorldFall already started!");
            return;
        }

        int time = 5;
        if (args.length > 1) {
            try {
                time = Integer.parseInt(args[1]);
                if (time < 0) {
                    player.sendMessage(wfr.pluginPrefix + "§4Time cannot be negative!");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(wfr.pluginPrefix + "§4Invalid time format!");
                return;
            }
        }

        // Check if there's already a scheduled task and cancel it if it exists
        // Filter the pending tasks to only select the start tasks owned by the plugin (we don't want to cancel the scoreboard task)
        if (startTask != null) {
            startTask.cancel();
            player.sendMessage(wfr.pluginPrefix + "§6Previous start task cancelled!");
        }

        Bukkit.broadcastMessage(wfr.pluginPrefix + "§6WorldFall starting in §b" + time + " seconds");
        startTask = Bukkit.getScheduler().runTaskLater(wfr, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode() == GameMode.ADVENTURE) p.setGameMode(GameMode.SURVIVAL);
            }
            wfr.wfActive = true;
            Bukkit.broadcastMessage(wfr.pluginPrefix + "§aWorldFall started!");
        }, time * 20L);
    }

    private void stopWorldFall(Player player) {
        if (!player.hasPermission("wf.action.stop")) {
            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
            return;
        }

        if (!wfr.wfStarted()) {
            player.sendMessage(wfr.pluginPrefix + "§4WorldFall not active. Use §b/worldfall start §4to start.");
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL) p.setGameMode(GameMode.ADVENTURE);
        }

        wfr.wfActive = false;
        Bukkit.broadcastMessage(wfr.pluginPrefix + "§aWorldFall stopped! You may now relax (for now)");
    }

    private void reloadConfig(Player player) {
        if (!player.hasPermission("wf.config.reload")) {
            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
            return;
        }

        wfr.reloadConfig();
        player.sendMessage(wfr.pluginPrefix + "Config reloaded!");
    }

    private void handleScoreboard(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(wfr.pluginPrefix + "§4Usage: §6/worldfall scoreboard <enable|disable>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable" -> toggleScoreboard(player, true);
            case "disable" -> toggleScoreboard(player, false);
            default -> player.sendMessage(wfr.pluginPrefix + "§4Usage: §6/worldfall scoreboard <enable|disable>");
        }
    }

    private void toggleScoreboard(Player player, boolean enable) {
        String permission = enable ? "wf.config.scoreboard.enable" : "wf.config.scoreboard.disable";
        if (!player.hasPermission(permission)) {
            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
            return;
        }

        boolean currentState = wfr.getConfig().getBoolean("scoreboard.enabled");
        if (currentState == enable) {
            player.sendMessage(wfr.pluginPrefix + "§6Scoreboard already " + (enable ? "active" : "inactive") + "! Use §b/worldfall scoreboard " + (enable ? "disable" : "enable") + " §6to change it or change the plugin config.");
            return;
        }

        wfr.getConfig().set("scoreboard.enabled", enable);
        wfr.saveConfig();
        player.sendMessage(wfr.pluginPrefix + "§aScoreboard " + (enable ? "enabled" : "disabled") + "!");
    }

    private void teleportPlayers(Player player, String[] args) {
        if (!player.hasPermission("wf.action.tp")) {
            player.sendMessage(wfr.pluginPrefix + "§4You have no permission to use this command.");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                GameMode gm = p.getGameMode();
                if (gm != GameMode.ADVENTURE && (!wfr.wfStarted() || gm != GameMode.SURVIVAL)) continue;
                p.addPotionEffects(List.of(
                        new PotionEffect(PotionEffectType.SLOW, 1000, 255),
                        new PotionEffect(PotionEffectType.BLINDNESS, 1000, 255)
                ));
                p.setWalkSpeed(0);
                p.setGravity(false);
                p.teleport(player);

                p.setGravity(true);
                p.removePotionEffect(PotionEffectType.SLOW);
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                p.setWalkSpeed(0.2f);
                p.sendMessage(wfr.pluginPrefix + "§aTeleportation successful!");
            }
        } else {
            player.sendMessage(wfr.pluginPrefix + "§6Are you sure you want to teleport all players to you?");
            player.sendMessage(wfr.pluginPrefix + "§6Type §b/worldfall tp confirm §6to confirm");
        }
    }

    /**
     * Handle tab completion for the main command and its subcommands.
     * @param commandSender Command sender
     * @param command Command
     * @param s Command label
     * @param args Command arguments
     * @return List of possible tab completions
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> subcommands;
        List<String> secondSubcommands;

        // Check for the number of arguments
        switch (args.length) {
            case 1:
                // Display subcommands for the main command
                // Check for permissions before displaying certain commands
                subcommands = new ArrayList<>();
                subcommands.add("cmd");
                subcommands.add("version");
                subcommands.add("status");
                if (commandSender.hasPermission("wf.action.start")) subcommands.add("start");
                if (commandSender.hasPermission("wf.action.stop")) subcommands.add("stop");
                if (commandSender.hasPermission("wf.config.reload")) subcommands.add("reload");
                if (commandSender.hasPermission("wf.config.scoreboard.enable") || commandSender.hasPermission("wf.config.scoreboard.disable")) subcommands.add("scoreboard");
                if (commandSender.hasPermission("wf.action.tp")) subcommands.add("tp");
                return subcommands;
            case 2:
                // Display subcommands for the scoreboard command
                // Check for permissions before displaying certain commands
                secondSubcommands = new ArrayList<>();
                // Check for the first argument to display the correct subcommands
                switch (args[0].toLowerCase()) {
                    case "start":
                        if (commandSender.hasPermission("wf.action.start")) secondSubcommands.add("time");
                        break;
                    case "scoreboard":
                        if (commandSender.hasPermission("wf.config.scoreboard.enable")) secondSubcommands.add("enable");
                        if (commandSender.hasPermission("wf.config.scoreboard.disable")) secondSubcommands.add("disable");
                        break;
                    case "tp":
                        if (commandSender.hasPermission("wf.action.tp")) secondSubcommands.add("confirm");
                        break;
                    default:
                        // Just return null if the first argument is not recognized
                        return null;
                }
                return secondSubcommands;
            default:
                // Just return null if the number of arguments is not 1 or 2
                return null;
        }
    }
}
