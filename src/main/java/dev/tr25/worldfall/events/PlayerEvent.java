package dev.tr25.worldfall.events;

import dev.tr25.worldfall.WorldFall;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class PlayerEvent implements Listener {
    private final WorldFall wfr;
    HashMap<String, Boolean> playerHasMoved = new HashMap<>();

    /**
     * Constructor for PlayerEvent class
     * @param wfr Main plugin class
     */
    public PlayerEvent(WorldFall wfr) {
        this.wfr = wfr;
    }

    /**
     * Getter for player name attribute
     * @param player Player
     * @return Player name
     */
    private String getPlayerName(Player player) {
        Component playerNameComponent = player.displayName();
        return ((TextComponent) playerNameComponent).content();
    }

    /**
     * Player move event
     * @param event Player has moved
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        String name = getPlayerName(event.getPlayer());
        /* WorldFall active in the server */
        if (wfr.wfStarted()) {
            int fromX = event.getFrom().getBlockX();
            int fromZ = event.getFrom().getBlockZ();

            int toX = event.getTo().getBlockX();
            int toZ = event.getTo().getBlockZ();
            boolean hasMoved = (fromX != toX) | (fromZ != toZ);

            /* Player changed position */
            if (hasMoved) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    World world = event.getPlayer().getWorld();
                    for (int i = -64; i < 320; i++) {
                        Block block = world.getBlockAt(fromX, i, fromZ);
                        if (block.getType() != Material.END_PORTAL_FRAME) {
                            block.setType(Material.AIR);
                        }
                    }
                }
                playerHasMoved.put(name, true);
            } else {
                playerHasMoved.put(name, false);
            }
        } else {
            playerHasMoved.put(name, false);
        }

    }

    /**
     * Player dimension change event
     * @param event Player changed dimension
     */
    @EventHandler
    public void onChangeDimension(PlayerChangedWorldEvent event) {
        playerHasMoved.put(getPlayerName(event.getPlayer()), false);
    }

    /**
     * Player server join event
     * @param event Player entered the server
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if plugin is active
        if (wfr.wfStarted()) {
            // Check if player is not in survival mode
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                // Notify player that they are not in survival mode
                event.getPlayer().sendMessage(wfr.pluginPrefix + "&6You are not in survival mode. &cWorldFall &6will not work for you.");
                event.getPlayer().sendMessage(wfr.pluginPrefix + "&6Please switch to survival mode to use &cWorldFall&6.");
            }
        } else {
            // Check if player is not in adventure mode
            if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                // Notify player that they are not in adventure mode
                event.getPlayer().sendMessage(wfr.pluginPrefix + "&6You are not in adventure mode. &cWorldFall &6will not work for you.");
                event.getPlayer().sendMessage(wfr.pluginPrefix + "&6Please switch to adventure mode to use &cWorldFall&6.");
            }
        }

        playerHasMoved.put(getPlayerName(event.getPlayer()), false);
    }
}
