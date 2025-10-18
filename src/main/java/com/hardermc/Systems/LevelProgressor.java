package com.hardermc.Systems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hardermc.HarderMC;

public class LevelProgressor implements Listener {
    private static final String HIGHEST_LEVEL_REACHED_KEY = "HIGHEST_LEVEL_REACHED";
    private static final int TARGET_LEVEL = 25;
    private final HarderMC plugin;
    public int level = 1;
    public int highestLevelReached = 1;

    public LevelProgressor(HarderMC plugin) {
        this.plugin = plugin;
        this.highestLevelReached = plugin.getConfig().getInt(HIGHEST_LEVEL_REACHED_KEY, 1);
        determineLevel();
    }

    private void determineLevel() {
        int highestPlayerLevel = 1;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int playerLevel = player.getLevel();
            if (playerLevel > highestPlayerLevel)
                highestPlayerLevel = playerLevel;
        }

        setLevel(Math.max(highestPlayerLevel, highestLevelReached));
    }

    private void setLevel(int newLevel) {
        plugin.getLogger().info(String.format("Setting blood moon level to %d", newLevel));
        this.level = newLevel;

        double multiplier = 1.0 + ((double) newLevel - 1) / (TARGET_LEVEL - 1);
        plugin.statMultiplier.globalMultiplier = multiplier;
        Bukkit.broadcastMessage(String.format("Blood moon level set to %d (%.2fx difficulty)", newLevel, multiplier));

        highestLevelReached = Math.max(highestLevelReached, newLevel);
        plugin.getConfig().set(HIGHEST_LEVEL_REACHED_KEY, highestLevelReached);
        plugin.saveConfig();
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        determineLevel();
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
