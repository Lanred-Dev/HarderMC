package com.hardermc;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

// Intend to do more with this class in the future

public class EventScheduler implements Listener {
    private final HarderMC plugin;
    private boolean wasNight = false;

    public EventScheduler(HarderMC plugin) {
        this.plugin = plugin;
        startTimeWatcher();
    }

    public void startTimeWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld("world");

                if (world == null)
                    return;

                long time = world.getTime();
                boolean isNight = time >= 13000 && time <= 23000;

                if (isNight && !wasNight) {
                    plugin.getLogger().info("Night has begun");
                    plugin.bloodMoon.newNight();
                } else if (!isNight && wasNight) {
                    plugin.getLogger().info("Day has begun");
                    plugin.bloodMoon.newDay();
                }

                wasNight = isNight;
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }
}
