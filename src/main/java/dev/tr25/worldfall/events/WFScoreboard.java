package dev.tr25.worldfall.events;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.text.NumberFormat;
import java.util.List;

public class WFScoreboard {
    private final WorldFall wfr;
    int taskID;

    public WFScoreboard(WorldFall wfr) {
        this.wfr = wfr;
    }

    public void createScoreboard (int tickReload) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(wfr, () -> {
            FileConfiguration config = wfr.getConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player, config);
            }
        }, 0, tickReload);
    }

    public void updateScoreboard(Player player, FileConfiguration config) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

        if (config.getBoolean("scoreboard.enabled")) {
            // Parse title with both Legacy and MiniMessage parsers
            String titleRaw = config.getString("scoreboard.title", "&l&k[&r&6&lWorldFall&r&l&k]&r");
            Component title = parseText(titleRaw, miniMessage, legacySerializer);

            // Register objective
            Objective objective = scoreboard.registerNewObjective("WorldFall", Criteria.DUMMY, title, RenderType.INTEGER);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<String> lines = config.getStringList("scoreboard.text");
            double x = player.getLocation().getX();
            double y = player.getLocation().getY();
            double z = player.getLocation().getZ();

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);

            for (int i = 0; i < lines.size(); i++) {
                // Replace placeholders in text
                String rawLine = lines.get(i)
                    .replace("%coord_x%", nf.format(x))
                    .replace("%coord_y%", nf.format(y))
                    .replace("%coord_z%", nf.format(z))
                    .replace("%wf_status%", (wfr.isWfActive() ? "<green><bold>ACTIVE</bold></green>" : "<red><bold>INACTIVE</bold></red>"))
                    .replace("%player_name%", player.getName());

                Component lineComponent = parseText(rawLine, miniMessage, legacySerializer);
                String lineString = legacySerializer.serialize(lineComponent).replace('&', '§');

                // Add line to scoreboard
                Score score = objective.getScore(lineString);
                score.setScore(lines.size() - (i));
            }
        }

        player.setScoreboard(scoreboard);
    }

    private Component parseText(String rawText, MiniMessage miniMessage, LegacyComponentSerializer legacySerializer) {
        Component legacy = legacySerializer.deserialize(rawText);
        String mmString = miniMessage.serialize(legacy).replace("\\", "");
        return miniMessage.deserialize(mmString);
    }
}
