package com.hardermc.Effects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;

public class TNTRain {
    private final static int TNT_RAIN_RADIUS = 30;
    private final HarderMC plugin;

    public TNTRain(HarderMC plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.bloodMoon.isActive) {
                    cancel();
                    return;
                }

                World world = Bukkit.getWorld("world");

                if (world == null) {
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld() != world) {
                        continue;
                    }

                    int[] position = plugin.utils.getRandomPositionAroundPlayer(player, TNT_RAIN_RADIUS);

                    if (!world.getBlockAt(position[0], position[1], position[2]).isEmpty()) {
                        continue;
                    }

                    world.spawnEntity(new org.bukkit.Location(world, position[0], position[1], position[2]),
                            org.bukkit.entity.EntityType.PRIMED_TNT);
                }
            }
        }.runTaskTimer(plugin, 0L, (long) Math.max(35.0 / plugin.statMultiplier.globalMultiplier, 1L));
    }
}
