package com.hardermc.Events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;
import com.hardermc.Utils;
import com.hardermc.Objects.SchedulerEvent;
import com.hardermc.Systems.Scheduler.TimeOfDay;

public class BloodMoon extends SchedulerEvent implements Listener {
    private final static int LIGHTNING_STORM_RADIUS = 30;
    private final static int LIGHTNING_STORM_FREQUENCY = Utils.secondsToTicks(5);
    private final static int TNT_RAIN_RADIUS = 30;
    private final static int TNT_RAIN_FREQUENCY = Utils.secondsToTicks(2.2);
    private static final double GLOBAL_MOB_MULTIPLIER = 10.0;
    private final Map<Player, Integer> kills = new HashMap<>();
    private final Map<Player, Integer> deaths = new HashMap<>();
    private boolean allPlayersStayedAlive = true;

    public BloodMoon(HarderMC plugin) {
        super(plugin);
        initialize();
    }

    @Override
    public int EVENT_INTERVAL() {
        return 7;
    }

    @Override
    public TimeOfDay EVENT_STARTS_AT() {
        return TimeOfDay.NIGHT;
    }

    @Override
    public TimeOfDay EVENT_ENDS_AT() {
        return TimeOfDay.DAY;
    }

    @Override
    public int EVENT_LASTS_FOR() {
        return 0;
    }

    @Override
    public String EVENT_ID() {
        return "BLOOD_MOON";
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("The Blood Moon is rising...");

        allPlayersStayedAlive = true;

        kills.clear();
        deaths.clear();

        plugin.mobHandler.globalMultiplier.add(GLOBAL_MOB_MULTIPLIER);
        plugin.fearSystem.minimumFearLevel = 3.0;

        startLightningStorm();
        startTNTRain();
    }

    @Override
    public void end() {
        Bukkit.broadcastMessage("The Blood Moon has ended.");

        if (allPlayersStayedAlive) {
            Bukkit.broadcastMessage("All players survived the Blood Moon rewards will be distributed.");

            for (Player player : Bukkit.getOnlinePlayers()) {
                for (ItemStack reward : plugin.rewardService.getRewards(1, 2)) {
                    player.getWorld().dropItemNaturally(player.getLocation(), reward);
                }
            }
        }

        plugin.mobHandler.globalMultiplier.remove(GLOBAL_MOB_MULTIPLIER);
        plugin.fearSystem.minimumFearLevel = 0.0;

        broadcastStats();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isActive())
            return;

        Entity entity = event.getEntity();

        if (entity.isDead() || !(entity instanceof LivingEntity victim)) {
            return;
        }

        Entity damager = event.getDamager();

        if (victim.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        if (entity instanceof Player player) {
            allPlayersStayedAlive = false;
            deaths.put(player, deaths.getOrDefault(player, 0) + 1);
        } else if (entity instanceof Monster && damager instanceof Player player) {
            kills.put(player, kills.getOrDefault(player, 0) + 1);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isActive()) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot sleep during a Blood Moon.");
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster mob) || !isActive())
            return;

        mob.setCustomName(String.format("Blood Moon %s", mob.getCustomName()));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));

        Utils.haveMobTargetNearestPlayer(mob);
    }

    @EventHandler
    public void onSkeletonShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton) || !isActive()
                || !(event.getProjectile() instanceof Arrow arrow))
            return;

        arrow.setFireTicks(200);
        Arrow extraArrow = skeleton.launchProjectile(Arrow.class);
        extraArrow.setFireTicks(200);
        extraArrow.setVelocity(arrow.getVelocity().multiply(1.2));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive())
            return;

        event.getPlayer().sendMessage("Beware... a Blood Moon is currently active.");
    }

    private void broadcastStats() {
        Bukkit.broadcastMessage("Heres how each player did during the Blood Moon:");

        for (Player player : kills.keySet()) {
            Integer playerKills = kills.getOrDefault(player, 0);
            Integer playerDeaths = deaths.getOrDefault(player, 0);
            Bukkit.broadcastMessage(
                    String.format("%s - Kills: %d, Deaths: %d", player.getName(), playerKills, playerDeaths));
        }
    }

    private void startLightningStorm() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
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

                    int[] position = Utils.getRandomPositionAroundPlayer(player, LIGHTNING_STORM_RADIUS);
                    world.strikeLightningEffect(new org.bukkit.Location(world, position[0], position[1], position[2]));
                }
            }
        }.runTaskTimer(plugin, 0L, LIGHTNING_STORM_FREQUENCY);
    }

    private void startTNTRain() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
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

                    int[] position = Utils.getRandomPositionAroundPlayer(player, TNT_RAIN_RADIUS);

                    if (!world.getBlockAt(position[0], position[1], position[2]).isEmpty()) {
                        continue;
                    }

                    world.spawnEntity(new org.bukkit.Location(world, position[0], position[1], position[2]),
                            org.bukkit.entity.EntityType.TNT);
                }
            }
        }.runTaskTimer(plugin, 0L, (long) Math.max(TNT_RAIN_FREQUENCY / plugin.levelSystem.levelMultiplier, 1L));
    }
}
