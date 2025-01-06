package dev.tr25.worldfall.commands;

import dev.tr25.worldfall.WorldFall;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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
        if (!(commandSender instanceof Player player)) {
            if (Bukkit.getLogger().isLoggable(java.util.logging.Level.INFO)) Bukkit.getLogger().info(wfr.pluginName + "\u001B[31m > Command not available in Console!\u001B[37m");
            return false;
        }

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
            default -> sendMessage(player, "§4Unknown command. Use §6/worldfall cmd §4for a list of commands");
        }

        return true;
    }

    /**
     * Displays information about the WorldFall plugin to the specified player.
     *
     * @param player The player to whom the information will be displayed
     */
    private void displayInfo(Player player) {
        sendMessage(player, String.join("\n",
                "§6---------------------------------",
                "§6§lWorldFall §aplugin by TR25",
                "§aVersion: §b" + wfr.version,
                "§aUse §b/worldfall cmd §ato see the command list",
                "§aAliases: §b/wf",
                "§6---------------------------------"
        ));
    }

    /**
     * Displays a list of available commands to the specified player.
     * The list of commands is based on the permissions of the player.
     *
     * @param player The player to whom the command list will be displayed
     */
    private void displayCommandList(Player player) {
        List<String> commands = new ArrayList<>(List.of(
                "§6---------------------------------",
                "§aCommand List",
                "§6/worldfall§a: Main Command",
                "§6/worldfall cmd§a: Displays this list",
                "§6/worldfall version§a: Get the plugin version",
                "§6/worldfall status§a: Check WorldFall status"
        ));
        if (hasPermission(player, "wf.config.reload")) commands.add("§6/worldfall reload§a: Reloads the plugin config");
        if (hasPermission(player, "wf.action.start")) commands.add("§6/worldfall start§a: Starts WorldFall in 5 seconds (or specified time)");
        if (hasPermission(player, "wf.action.stop")) commands.add("§6/worldfall stop§a: Stops WorldFall");
        if (hasPermission(player, "wf.config.scoreboard.enable")) commands.add("§6/worldfall scoreboard enable§a: Enables the sidebar");
        if (hasPermission(player, "wf.config.scoreboard.disable")) commands.add("§6/worldfall scoreboard disable§a: Disables the sidebar");
        if (hasPermission(player, "wf.action.tp")) commands.add("§6/worldfall tp§a: Teleport all players to you");
        commands.add("§6---------------------------------");

        sendMessage(player, String.join("\n", commands));
    }

    /**
     * Sends a message to the specified player displaying the current version of the WorldFall plugin.
     *
     * @param player The player to whom the version message will be sent
     */
    private void displayVersion(Player player) {
        sendMessage(player, "WorldFall for Paper (version " + wfr.version + ")");
    }

    /**
     * Displays the current status of the WorldFall plugin to the specified player.
     *
     * @param player The player to whom the status message will be sent
     */
    private void displayStatus(Player player) {
        sendMessage(player, "WorldFall Status: " + (wfr.isWfActive() ? "§a§lACTIVE" : "§c§lINACTIVE"));
    }

    /**
     * Starts the WorldFall game with a specified time delay.
     *
     * @param player The player who initiated the command
     * @param args   The command arguments. The second argument (if present) specifies the countdown time in seconds.
     */
    private void startWorldFall(Player player, String[] args) {
        if (!hasPermission(player, "wf.action.start")) return;
        if (wfr.isWfActive()) {
            sendMessage(player, "§6WorldFall already started!");
            return;
        }

        int time = parseTime(args, player);
        if (time < 0) return;

        cancelPreviousStartTask();
        scheduleStartTask(time);
    }

    /**
     * Parses the time from the given arguments.
     * If the time argument is not provided, returns a default value of 5.
     * If the time argument is invalid or negative, sends an error message to the player and returns -1.
     *
     * @param args The command arguments
     * @param player The player executing the command
     * @return The parsed time, or -1 if the time is invalid, or 5 if no time argument is provided
     */
    private int parseTime(String[] args, Player player) {
        if (args.length > 1) {
            try {
                int time = Integer.parseInt(args[1]);
                if (time < 0) throw new IllegalArgumentException("Negative time");
                return time;
            } catch (Exception e) {
                sendMessage(player, "§4Invalid time argument. Please provide a positive integer.");
                return -1;
            }
        }
        return 5;
    }

    /**
     * Cancels the previously scheduled start task if it exists.
     * This method checks if the start task is not null and cancels it.
     */
    private void cancelPreviousStartTask() {
        if (startTask != null) {
            startTask.cancel();
            startTask = null;
        }
    }

    /**
     * Schedules a task to start the WorldFall game after a specified delay.
     *
     * @param time The time delay in seconds
     */
    private void scheduleStartTask(int time) {
        Bukkit.getServer().broadcast(wfr.pluginPrefix.append(Component.text("§6WorldFall starting in §b" + time + " seconds")));
        startTask = Bukkit.getScheduler().runTaskLater(wfr, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode() == GameMode.ADVENTURE) p.setGameMode(GameMode.SURVIVAL);
            }
            wfr.setWfActive(true);
            Bukkit.getServer().broadcast(wfr.pluginPrefix.append(Component.text("§aWorldFall started!")));
        }, time * 20L);
    }

    /**
     * Stops WorldFall.
     *
     * @param player The player who initiated the command
     */
    private void stopWorldFall(Player player) {
        if (!hasPermission(player, "wf.action.stop")) return;
        if (!wfr.isWfActive()) {
            sendMessage(player, "§4WorldFall not active. Use §b/worldfall start §4to start.");
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL) p.setGameMode(GameMode.ADVENTURE);
        }

        wfr.setWfActive(false);
        Bukkit.getServer().broadcast(wfr.pluginPrefix.append(Component.text("§aWorldFall stopped! You may now relax (for now)")));
    }

    /**
     * Reloads the WorldFall plugin configuration.
     *
     * @param player The player who initiated the command
     */
    private void reloadConfig(Player player) {
        if (!hasPermission(player, "wf.config.reload")) return;
        wfr.reloadConfig();
        sendMessage(player, "Config reloaded!");
    }

    /**
     * Handles the scoreboard command.
     * This method enables or disables the scoreboard based on the specified argument.
     *
     * @param player The player executing the command
     * @param args The arguments passed with the command
     * @see #toggleScoreboard(Player, boolean)
     */
    private void handleScoreboard(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "§4Usage: §6/worldfall scoreboard <enable|disable>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable" -> toggleScoreboard(player, true);
            case "disable" -> toggleScoreboard(player, false);
            default -> sendMessage(player, "§4Usage: §6/worldfall scoreboard <enable|disable>");
        }
    }

    /**
     * Toggle the scoreboard based on the specified enable flag.
     *
     * @param player The player executing the command
     * @param enable The flag to enable or disable the scoreboard
     */
    private void toggleScoreboard(Player player, boolean enable) {
        String permission = enable ? "wf.config.scoreboard.enable" : "wf.config.scoreboard.disable";
        if (!hasPermission(player, permission)) return;

        boolean currentState = wfr.getConfig().getBoolean("scoreboard.enabled");
        if (currentState == enable) {
            sendMessage(player, "§6Scoreboard already " + (enable ? "active" : "inactive") + "!");
            return;
        }

        wfr.getConfig().set("scoreboard.enabled", enable);
        wfr.saveConfig();
        sendMessage(player, "§aScoreboard " + (enable ? "enabled" : "disabled") + "!");
    }

    /**
     * Teleports all eligible players to the command sender.
     * Eligible players are those in survival mode when WorldFall is active or adventure mode when WorldFall is inactive.
     * The command sender must confirm the teleportation by typing "/worldfall tp confirm".
     * If the confirmation argument is not provided, a confirmation message is sent to the command sender.
     *
     * @param player The player executing the command
     * @param args The command arguments. The second argument (if present) should be "confirm" to confirm teleportation
     */
    private void teleportPlayers(Player player, String[] args) {
        if (!hasPermission(player, "wf.action.tp")) return;
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                GameMode gm = p.getGameMode();
                if (gm != GameMode.ADVENTURE && (!wfr.isWfActive() || gm != GameMode.SURVIVAL)) continue;
                p.teleport(player);
                sendMessage(p, "§6Teleported to §b" + player.getName() + "!");
            }
        } else {
            sendMessage(player, "§6Are you sure you want to teleport all players to you?");
            sendMessage(player, "§6Type §b/worldfall tp confirm §6to confirm.");
        }
    }

    /**
     * Checks if the player has the specified permission.
     * If the player does not have the permission, a message is sent to the player
     * indicating that they do not have permission to use the command.
     *
     * @param player The player whose permissions are being checked
     * @param permission The permission string to check against the player's permissions
     * @return True if the player has the specified permission, false otherwise
     */
    private boolean hasPermission(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            sendMessage(player, "§4You do not have permission to use this command!");
            return false;
        }
        return true;
    }

    /**
     * Sends a message to the specified player with the plugin's prefix.
     *
     * @param player The player to send the message to
     * @param message The message to be sent
     */
    private void sendMessage(Player player, String message) {
        player.sendMessage(wfr.pluginPrefix.append(Component.text(message)));
    }

    /**
     * Handle tab completion for the main command and its subcommands.
     * @param sender Command sender
     * @param command Command
     * @param label Command label
     * @param args Command arguments
     * @return List of possible tab completions
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            addMainCommandOptions(sender, options);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("scoreboard")) {
            addScoreboardOptions(sender, options);
        }
        return filterOptions(args[args.length - 1], options);
    }

    /**
     * Adds main command options to the provided list based on the sender's permissions.
     *
     * @param sender The command sender
     * @param options The list of options to add main command options to
     */
    private void addMainCommandOptions(@NotNull CommandSender sender, List<String> options) {
        options.addAll(List.of("cmd", "version", "status"));
        if (sender.hasPermission("wf.action.start")) options.add("start");
        if (sender.hasPermission("wf.action.stop")) options.add("stop");
        if (sender.hasPermission("wf.config.reload")) options.add("reload");
        if (sender.hasPermission("wf.config.scoreboard.enable") || sender.hasPermission("wf.config.scoreboard.disable")) options.add("scoreboard");
        if (sender.hasPermission("wf.action.tp")) options.add("tp");
    }

    /**
     * Adds scoreboard options to the provided list based on the sender's permissions.
     *
     * @param sender The command sender
     * @param options The list of options to add scoreboard options to
     */
    private void addScoreboardOptions(@NotNull CommandSender sender, List<String> options) {
        if (sender.hasPermission("wf.config.scoreboard.enable")) options.add("enable");
        if (sender.hasPermission("wf.config.scoreboard.disable")) options.add("disable");
    }

    /**
     * Filters a list of options to include only those that start with the given input string.
     *
     * @param input The input string to filter options by. The comparison is case-insensitive
     * @param options The list of options to filter
     * @return A list of options that start with the input string
     */
    private List<String> filterOptions(String input, List<String> options) {
        return options.stream().filter(opt -> opt.startsWith(input.toLowerCase())).toList();
    }
}
