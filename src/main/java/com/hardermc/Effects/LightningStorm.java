package com.hardermc.Effects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;

public class LightningStorm {
    private final static int LIGHTNING_STORM_RADIUS = 30;
    private final HarderMC plugin;

    public LightningStorm(HarderMC plugin) {
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

                    int[] position = plugin.utils.getRandomPositionAroundPlayer(player, LIGHTNING_STORM_RADIUS);
                    world.strikeLightningEffect(new org.bukkit.Location(world, position[0], position[1], position[2]));
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }
}
