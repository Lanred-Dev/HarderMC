package com.hardermc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LevelProgressor implements Listener {
    private static final int TARGET_LEVEL = 25;
    private final HarderMC plugin;
    public int level = 1;

    public LevelProgressor(HarderMC plugin) {
        this.plugin = plugin;
        determineLevel();
    }

    private void determineLevel() {
        int highestLevel = 1;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int playerLevel = player.getLevel();
            if (playerLevel > highestLevel)
                highestLevel = playerLevel;
        }

        setLevel(highestLevel);
    }

    private void setLevel(int newLevel) {
        plugin.getLogger().info(String.format("Setting blood moon level to %d", newLevel));
        this.level = newLevel;

        double multiplier = 1.0 + ((double) newLevel - 1) / (TARGET_LEVEL - 1);
        plugin.statMultiplier.globalMultiplier = multiplier;
        Bukkit.broadcastMessage(String.format("Blood moon level set to %d (%.2fx difficulty)", newLevel, multiplier));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("Determining level due to player join");
        determineLevel();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLogger().info("Determining level due to player quit");
        determineLevel();
    }
}
