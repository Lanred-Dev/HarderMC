package com.hardermc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

/** Utility class for common helper functions */
public final class Utils {
    public static final int TICKS_PER_SECOND = 20;
    public static final double MAX_ENTITY_HEALTH = 1024.0;
    public static final double MAX_ENTITY_DAMAGE = 4086.0;
    public static final double MAX_ENTITY_SPEED = 3.0;

    private Utils() {
    }

    public static int secondsToTicks(double seconds) {
        return (int) (seconds * TICKS_PER_SECOND);
    }

    public static <T> T randomEntryFromArray(T[] array) {
        return array[(int) (Math.random() * array.length)];
    }

    public static int[] getRandomPositionAroundPlayer(Player player, int radius) {
        int x = player.getLocation().getBlockX() + (int) (Math.random() * radius) - radius / 2;
        int z = player.getLocation().getBlockZ() + (int) (Math.random() * radius) - radius / 2;
        int y = player.getLocation().getBlockY() + 60;
        return new int[] { x, y, z };
    }

    public static void haveMobTargetNearestPlayer(Mob mob) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            double distance = player.getLocation().distanceSquared(mob.getLocation());

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        mob.setTarget(nearestPlayer);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}