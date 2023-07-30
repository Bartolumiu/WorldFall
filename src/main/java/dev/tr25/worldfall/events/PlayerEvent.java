package dev.tr25.worldfall.events;

import dev.tr25.worldfall.WorldFall;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    HashMap<String, Boolean> playerHasMoved = new HashMap<String, Boolean>();

    /**
     * Constructor for PlayerEvent class
     * @param wfr Main plugin class
     */
    public PlayerEvent (WorldFall wfr) {
        this.wfr = wfr;
    }

    /**
     * Getter for player name attribute
     * @param player Player
     * @return Player name
     */
    private String getPlayerName(Player player) {
        return player.getDisplayName();
    }

    /**
     * Player move event
     * @param event Player has moved
     */
    @EventHandler
    public void onMove (PlayerMoveEvent event) {
        String name = getPlayerName(event.getPlayer());

        /* WorldFall active in the server */
        if (wfr.wfActive) {
            Location from = event.getFrom();
            Location to = event.getTo();

            int fromX = from.getBlockX();
            int fromZ = from.getBlockZ();

            assert to != null;
            int toX = to.getBlockX();
            int toZ = to.getBlockZ();

            boolean hasMoved = (fromX != toX) | (fromZ != toZ);

            /* Player changed position */
            if (hasMoved) {
                if (playerHasMoved.get(name) & event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
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
    public void onChangeDimension (PlayerChangedWorldEvent event) {
        playerHasMoved.put(getPlayerName(event.getPlayer()), false);
    }

    /**
     * Player server join event
     * @param event Player entered the server
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerHasMoved.put(getPlayerName(event.getPlayer()), false);
    }
}
