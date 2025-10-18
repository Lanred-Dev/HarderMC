package com.hardermc.Systems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.hardermc.HarderMC;

public class Level implements Listener {
    private final static int BASE_TARGET_LEVEL = 25;
    private static final String CURRENT_LEVEL_KEY = "level";
    private final HarderMC plugin;
    public int level;
    public double levelMultiplier;

    public Level(HarderMC plugin) {
        this.plugin = plugin;
        this.level = (int) plugin.serverDataService.get(CURRENT_LEVEL_KEY, 1);
        this.levelMultiplier = 1.0;
        determineLevelFromPlayers();
    }

    private void determineLevelFromPlayers() {
        HarderMC.LOGGER.info("Determining level based on online players");

        int highestPlayerLevel = 1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            highestPlayerLevel = Math.max(highestPlayerLevel, player.getLevel());
        }

        // This is required because players can loose XP
        if (highestPlayerLevel > this.level)
            setLevel(highestPlayerLevel);
    }

    public void setLevel(int newLevel) {
        // Remove old multiplier, this will remove the default entry of 1.0 as well on
        // startup (but that's fine)
        plugin.mobHandler.globalMultiplier.remove(levelMultiplier);

        level = newLevel;
        levelMultiplier = ((newLevel / BASE_TARGET_LEVEL) * 0.5) + 0.5;
        plugin.mobHandler.globalMultiplier.add(levelMultiplier);

        plugin.serverDataService.set(CURRENT_LEVEL_KEY, newLevel);
        HarderMC.LOGGER.info(String.format("Level set to %d", newLevel));
        Bukkit.broadcastMessage(String.format("Level set to %d (%.2fx difficulty)", newLevel, levelMultiplier));
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        determineLevelFromPlayers();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        determineLevelFromPlayers();
        event.getPlayer()
                .sendMessage(String.format("Current server level: %d (%.2fx difficulty)", level, levelMultiplier));
    }
}
