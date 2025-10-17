package com.hardermc;

import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BloodMoonHandler implements Listener {
    private static final int EVENT_SPAWN_RADIUS = 60;
    private static final int BLOOD_MOON_INTERVAL = 7;
    private static final String DAYS_SINCE_LAST_BLOOD_MOON_KEY = "daysSinceLastBloodMoon";
    private final HarderMC plugin;
    public boolean isActive = false;
    private int daysSinceLastBloodMoon = 0;

    public BloodMoonHandler(HarderMC plugin) {
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
        startTNTRain();
        startLightning();
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

    private int[] getRandomPositionAroundPlayer(Player player, int radius) {
        int x = player.getLocation().getBlockX() + (int) (Math.random() * radius) - radius / 2;
        int z = player.getLocation().getBlockZ() + (int) (Math.random() * radius) - radius / 2;
        int y = player.getLocation().getBlockY() + 60;
        return new int[] { x, y, z };
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isActive) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot sleep during a blood moon");
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
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isActive)
            event.getPlayer().sendMessage("The blood moon is currently active");
    }

    private void startTNTRain() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
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

                    int[] position = getRandomPositionAroundPlayer(player, EVENT_SPAWN_RADIUS);

                    if (!world.getBlockAt(position[0], position[1], position[2]).isEmpty()) {
                        continue;
                    }

                    world.spawnEntity(new org.bukkit.Location(world, position[0], position[1], position[2]),
                            org.bukkit.entity.EntityType.PRIMED_TNT);
                }
            }
        }.runTaskTimer(plugin, 0L, (long) Math.max(35.0 / plugin.statMultiplier.globalMultiplier, 1L));
    }

    private void startLightning() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
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

                    int[] position = getRandomPositionAroundPlayer(player, EVENT_SPAWN_RADIUS);
                    world.strikeLightningEffect(new org.bukkit.Location(world, position[0], position[1], position[2]));
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }
}
