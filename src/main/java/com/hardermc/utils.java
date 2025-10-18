package com.hardermc;

import org.bukkit.entity.Player;

public class utils {
    public static final int TICKS_PER_SECOND = 20;

    public int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public int[] getRandomPositionAroundPlayer(Player player, int radius) {
        int x = player.getLocation().getBlockX() + (int) (Math.random() * radius) - radius / 2;
        int z = player.getLocation().getBlockZ() + (int) (Math.random() * radius) - radius / 2;
        int y = player.getLocation().getBlockY() + 60;
        return new int[] { x, y, z };
    }
}
