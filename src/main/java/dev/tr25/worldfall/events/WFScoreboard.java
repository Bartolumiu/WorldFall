package dev.tr25.worldfall.events;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.text.NumberFormat;
import java.util.List;

public class WFScoreboard {
    private final WorldFall wfr;
    int taskID;

    public WFScoreboard (WorldFall wfr) {
        this.wfr = wfr;
    }

    public void createScoreboard (int tickReload) {
        BukkitScheduler schedule = Bukkit.getServer().getScheduler();
        taskID = schedule.scheduleSyncRepeatingTask(wfr, () -> {
            FileConfiguration config = wfr.getConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player, config);
            }
        }, 0, tickReload);
    }

    public void updateScoreboard (Player player, FileConfiguration config) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("WorldFall", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (config.getBoolean("scoreboard.enabled")) {
            objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title")));
            List<String> lines = config.getStringList("scoreboard.text");
            double x = player.getLocation().getX();
            double y = player.getLocation().getY();
            double z = player.getLocation().getZ();

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);


            for (int i = 0; i < lines.size(); i++) {
                Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', lines.get(i).replace("%coord_x%", nf.format(x)).replace("%coord_y%", nf.format(y)).replace("%coord_z%", nf.format(z)).replace("%wf_status%", (wfr.wfActive ? "§a§lACTIVE§r" : "§c§lINACTIVE§r")).replace("%player_name%", player.getName())));
                score.setScore(lines.size() - (i));
            }
        }

        player.setScoreboard(scoreboard);
    }
}
