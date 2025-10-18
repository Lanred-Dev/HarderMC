package com.hardermc.Systems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class StatMultiplier implements Listener {
    private static final double MAX_HEALTH = 1024.0;
    private static final double MAX_DAMAGE = 4086.0;
    private static final double MAX_SPEED = 10.0;
    public double globalMultiplier = 1.0;
    public final List<Double> playerDamageMultiplier = new ArrayList<>();
    public final List<Double> mobHealthMultiplier = new ArrayList<>();
    public final List<Double> mobDamageMultiplier = new ArrayList<>();
    public final List<Double> hungerRateMultiplier = new ArrayList<>();
    public final List<Double> mobSpawnRateMultiplier = new ArrayList<>();
    public final List<Double> mobSpeedMultiplier = new ArrayList<>();

    private double getTotalMultiplier(List<Double> multipliers, Optional<Double> max) {
        return Math.min(multipliers.stream().reduce(1.0, (a, b) -> a.doubleValue() * b.doubleValue()).doubleValue(),
                max.orElse(Double.MAX_VALUE)) * globalMultiplier;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getCause() == EntityDamageEvent.DamageCause.FALL)
            return;

        event.setDamage(event.getDamage() * getTotalMultiplier(playerDamageMultiplier, Optional.empty()));
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)
                || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return;

        for (int index = 0; index < getTotalMultiplier(mobSpawnRateMultiplier, Optional.empty()); index++) {
            event.getLocation().getWorld().spawnEntity(event.getLocation(), event.getEntityType());
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster mob))
            return;

        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(
                mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()
                        * getTotalMultiplier(mobHealthMultiplier, Optional.of(MAX_HEALTH)));
        mob.setHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
                mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue()
                        * getTotalMultiplier(mobDamageMultiplier, Optional.of(MAX_DAMAGE)));
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue()
                        * getTotalMultiplier(mobSpeedMultiplier, Optional.of(MAX_SPEED)));
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setFoodLevel(
                (int) Math.max(event.getFoodLevel() - getTotalMultiplier(hungerRateMultiplier, Optional.empty()), 0));
    }
}
