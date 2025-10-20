package com.hardermc;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

/** Utility class for common helper functions */
public final class Utils {
    public static final int TICKS_PER_SECOND = 20;
    public static final Map<Attribute, Double> MAX_ATTRIBUTE_VALUES = Map.ofEntries(
            Map.entry(Attribute.MAX_HEALTH, 1024.0),
            Map.entry(Attribute.ATTACK_DAMAGE, 2048.0),
            Map.entry(Attribute.MOVEMENT_SPEED, 1024.0),
            Map.entry(Attribute.ATTACK_SPEED, 1024.0),
            Map.entry(Attribute.FOLLOW_RANGE, 2048.0),
            Map.entry(Attribute.KNOCKBACK_RESISTANCE, 1.0),
            Map.entry(Attribute.ARMOR, 30.0),
            Map.entry(Attribute.ARMOR_TOUGHNESS, 20.0),
            Map.entry(Attribute.LUCK, 1024.0));
    private static final Random random = new Random();

    private Utils() {
    }

    public static int secondsToTicks(double seconds) {
        return (int) (seconds * TICKS_PER_SECOND);
    }

    public static double sumArray(double[] array) {
        double sum = 0.0;

        for (double value : array) {
            sum += value;
        }

        return sum;
    }

    public static <T> T randomEntryFromArray(T[] array) {
        return array[(int) (Math.random() * array.length)];
    }

    public static <T> T randomEntryFromList(List<T> list) {
        return list.get((int) (Math.random() * list.size()));
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

    public static Location getRandomPositionAroundPlayer(Player player, int radius, int minDistance, int minY,
            int maxY) {
        int playerX = player.getLocation().getBlockX();
        int playerZ = player.getLocation().getBlockZ();

        int x, z;
        do {
            x = playerX + random.nextInt(radius * 2 + 1) - radius;
            z = playerZ + random.nextInt(radius * 2 + 1) - radius;
        } while (Math.abs(x - playerX) < minDistance && Math.abs(z - playerZ) < minDistance);

        int y = random.nextInt(maxY - minY + 1) + minY;
        return new Location(player.getWorld(), x + 0.5, y, z + 0.5);
    }
}