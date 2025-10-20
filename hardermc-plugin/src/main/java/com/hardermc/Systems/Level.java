package com.hardermc.Systems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.hardermc.HarderMC;

public class Level implements Listener {
    private final static int BASE_TARGET_LEVEL = 30;
    private static final String SAVED_LEVEL_KEY = "LEVEL";
    private final HarderMC plugin;
    public int level;
    public double levelMultiplier;
    private int accumulatedPlayerLevels = 0;

    public Level(HarderMC plugin) {
        this.plugin = plugin;

        double savedLevel = (double) plugin.serverDataService.get(SAVED_LEVEL_KEY, 0.0);
        setLevel((int) savedLevel);
    }

    private void determineLevelFromPlayers() {
        HarderMC.LOGGER.info("Determining level based on online players");

        accumulatedPlayerLevels = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            accumulatedPlayerLevels += player.getLevel();
        }

        // This is required because players can loose XP
        if (accumulatedPlayerLevels > this.level)
            setLevel(accumulatedPlayerLevels);
    }

    public void setLevel(int newLevel) {
        // Remove old multiplier, this will remove the default entry of 1.0 as well on
        // startup (but that's fine)
        plugin.mobHandler.globalMultiplier.remove(levelMultiplier);

        level = newLevel;
        levelMultiplier = (((double) newLevel / (double) BASE_TARGET_LEVEL) * 0.5) + 0.5;
        plugin.mobHandler.globalMultiplier.add(levelMultiplier);

        plugin.serverDataService.set(SAVED_LEVEL_KEY, newLevel);
        HarderMC.LOGGER.info(String.format("Level set to %d", newLevel));
        Bukkit.broadcastMessage(
                String.format("Level set to %d (%d%% difficulty)", newLevel, (int) (levelMultiplier * 100)));
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        determineLevelFromPlayers();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        determineLevelFromPlayers();
        event.getPlayer()
                .sendMessage(String.format("Current server level: %d (%d%% difficulty)", level,
                        (int) (levelMultiplier * 100)));
    }
}
