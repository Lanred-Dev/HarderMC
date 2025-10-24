package com.hardermc.Systems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;
import com.hardermc.Utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Fear implements Listener {
    private static final int DECAY_RATE = Utils.secondsToTicks(2);
    private static final double DECAY_AMOUNT = 1.0;
    private static final int BREAK_DURATION = Utils.secondsToTicks(60);
    private static final double BREAK_THRESHOLD = 10.0;
    private static final double MAX_LEVEL = 10.0;
    private static final double OUTSIDE_LEVEL_INCREASE = 0.05;
    private static final double PER_MOB_MULTIPLIER = 0.025;
    private final HarderMC plugin;
    private Map<Player, Double> fearLevels = new HashMap<>();
    private Map<Player, Boolean> inFear = new HashMap<>();
    private final NamespacedKey FEAR_KEY;
    private double ticksSinceLastDecay = 0;
    public double minimumFearLevel = 0.0;

    public Fear(HarderMC plugin) {
        this.plugin = plugin;
        FEAR_KEY = new NamespacedKey(plugin, "FEAR");

        new BukkitRunnable() {
            @Override
            public void run() {
                ticksSinceLastDecay += 1;

                boolean shouldFearDecay = ticksSinceLastDecay >= DECAY_RATE;
                if (shouldFearDecay)
                    ticksSinceLastDecay = 0;

                for (Player player : Bukkit.getOnlinePlayers()) {

                    long mobsNearby = player.getNearbyEntities(10, 10, 10).stream()
                            .filter(entity -> entity instanceof Monster)
                            .count();

                    double playerFear = fearLevels.getOrDefault(player, 0.0);

                    if (mobsNearby > 0)
                        playerFear += mobsNearby * PER_MOB_MULTIPLIER;

                    if (plugin.bloodMoonEvent.isActive() && player.getLocation().getBlock().getLightFromSky() > 0)
                        playerFear += OUTSIDE_LEVEL_INCREASE;

                    if (shouldFearDecay && !inFear.getOrDefault(player, false)
                            && playerFear - DECAY_AMOUNT >= minimumFearLevel)
                        playerFear -= DECAY_AMOUNT;

                    setPlayerFear(player, playerFear);

                    if (playerFear >= BREAK_THRESHOLD)
                        applyFearEffectsToPlayer(player);

                    updatePlayerFearBar(player, playerFear);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void applyFearEffectsToPlayer(Player player) {
        if (inFear.getOrDefault(player, false))
            return;

        HarderMC.LOGGER.info(String.format("Applying fear effects to %s", player.getName()));
        player.sendMessage("You feel a wave of fear wash over you...");

        inFear.put(player, true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, BREAK_DURATION, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, BREAK_DURATION, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, BREAK_DURATION, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BREAK_DURATION, 1, false, false));
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!inFear.containsKey(player))
                    return;

                inFear.put(player, false);
                fearLevels.put(player, Utils.clamp(BREAK_THRESHOLD / 2, minimumFearLevel, BREAK_THRESHOLD - 1.0));
            }
        }.runTaskLaterAsynchronously(plugin, BREAK_DURATION);
    }

    private void updatePlayerFearBar(Player player, double playerFear) {
        double fearPercent = Math.min(playerFear / MAX_LEVEL, 1.0);
        int totalBlocks = 10;
        int filled = (int) (fearPercent * totalBlocks);

        StringBuilder barVisual = new StringBuilder();
        for (int i = 0; i < totalBlocks; i++) {
            if (i < filled)
                barVisual.append(ChatColor.RED).append("█");
            else
                barVisual.append(ChatColor.DARK_GRAY).append("█");
        }

        TextComponent component = new TextComponent(ChatColor.BOLD + "Fear: " + barVisual.toString());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    public void setPlayerFear(Player player, double fearLevel) {
        fearLevels.put(player, Utils.clamp(fearLevel, minimumFearLevel, MAX_LEVEL));
    }

    public void resetPlayerFear(Player player) {
        setPlayerFear(player, minimumFearLevel);
        inFear.put(player, false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();
        fearLevels.put(player, data.getOrDefault(FEAR_KEY, PersistentDataType.DOUBLE, 0.0));
        inFear.put(player, false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(FEAR_KEY, PersistentDataType.DOUBLE, fearLevels.getOrDefault(player, 0.0));
        fearLevels.remove(player);
        inFear.remove(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        resetPlayerFear(player);
    }

    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        resetPlayerFear(player);
    }
}
