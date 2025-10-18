package com.hardermc.Mechanics;

import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.hardermc.HarderMC;

public class BloodMoon implements Listener {
    private static final int BLOOD_MOON_INTERVAL = 7;
    private static final String DAYS_SINCE_LAST_BLOOD_MOON_KEY = "daysSinceLastBloodMoon";
    private final HarderMC plugin;
    public boolean isActive = false;
    private int daysSinceLastBloodMoon = 0;

    public BloodMoon(HarderMC plugin) {
        this.plugin = plugin;
        this.daysSinceLastBloodMoon = plugin.getConfig().getInt(DAYS_SINCE_LAST_BLOOD_MOON_KEY, 0);
        attemptStart();
    }

    public void start() {
        Bukkit.broadcastMessage(String.format("The blood moon is rising... (level %d)", plugin.levelProgressor.level));
        plugin.getLogger().info(String.format("Blood moon started with level %d", plugin.levelProgressor.level));

        isActive = true;
        plugin.statMultiplier.mobDamageMultiplier.add(2.0);
        plugin.statMultiplier.mobHealthMultiplier.add(2.0);
        plugin.statMultiplier.mobSpawnRateMultiplier.add(4.0);
        plugin.tntRain.start();
        plugin.lightningStorm.start();
    }

    public void stop() {
        Bukkit.broadcastMessage("The blood moon has ended.");
        plugin.getLogger().info("Blood moon ended");

        isActive = false;
        plugin.statMultiplier.mobDamageMultiplier.remove(2.0);
        plugin.statMultiplier.mobHealthMultiplier.remove(2.0);
        plugin.statMultiplier.mobSpawnRateMultiplier.remove(4.0);

        plugin.playerStatTracker.displayThisBloodMoonStats();
    }

    public void newNight() {
        daysSinceLastBloodMoon++;
        attemptStart();
        plugin.getConfig().set(DAYS_SINCE_LAST_BLOOD_MOON_KEY, daysSinceLastBloodMoon);
        plugin.saveConfig();
    }

    public void newDay() {
        if (isActive)
            stop();
    }

    private void attemptStart() {
        if (daysSinceLastBloodMoon >= BLOOD_MOON_INTERVAL) {
            daysSinceLastBloodMoon = 0;
            start();
        } else {
            Bukkit.broadcastMessage(
                    String.format("%d until a blood moon", BLOOD_MOON_INTERVAL - daysSinceLastBloodMoon));
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isActive) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot sleep during a blood moon");
        plugin.playerFear.addFearToPlayer(event.getPlayer(), 1);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster mob) || !isActive) {
            return;
        }

        mob.setCustomName(String.format("Blood Moon %s", mob.getType().name()));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        mob.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_HELMET));
        mob.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE));
        mob.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_LEGGINGS));
        mob.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_BOOTS));
        mob.getEquipment().getHelmet().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        mob.getEquipment().getChestplate().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        mob.getEquipment().getLeggings().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        mob.getEquipment().getBoots().addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);

        // Target the nearest player
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

    @EventHandler
    public void onSkeletonShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton) || !isActive
                || !(event.getProjectile() instanceof Arrow arrow))
            return;

        arrow.setFireTicks(200);
        Arrow extraArrow = skeleton.launchProjectile(Arrow.class);
        extraArrow.setFireTicks(200);
        extraArrow.setVelocity(arrow.getVelocity().multiply(1.2));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isActive)
            event.getPlayer().sendMessage("The blood moon is currently active");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive)
            return;

        event.setDroppedExp(event.getDroppedExp() * 2);
    }
}
