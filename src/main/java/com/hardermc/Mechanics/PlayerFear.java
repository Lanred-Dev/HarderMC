package com.hardermc.Mechanics;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerFear implements Listener {
    private static final int FEAR_THRESHOLD = 6;
    private static final int FEAR_DECAY_RATE = 2;
    private static final int FEAR_DECAY_AMOUNT = 1;
    private static final double MOB_FEAR_MULTIPLIER = 0.6;
    private static final double MAX_FEAR_LEVEL = 10.0;
    private static final double OUTSIDE_FEAR_LEVEL_INCREASE = 0.3;
    private static final int FEAR_LASTS_FOR = 10;
    private Map<Player, Double> playerFearLevels = new HashMap<>();
    private Map<Player, Boolean> playersInFear = new HashMap<>();
    private final HarderMC plugin;
    private double ticksSinceFearDecay = 0;

    public PlayerFear(HarderMC plugin) {
        this.plugin = plugin;
        panicSimulation();
    }

    public void addFearToPlayer(Player player, double fearAmount) {
        double currentFear = playerFearLevels.getOrDefault(player, 0.0);
        currentFear += fearAmount;
        currentFear = Math.max(0.0, Math.min(currentFear, MAX_FEAR_LEVEL));
        playerFearLevels.put(player, currentFear);

        if (currentFear >= FEAR_THRESHOLD)
            applyFearEffectsToPlayer(player);

        updatePlayerFearBar(player, currentFear);
    }

    private void panicSimulation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ticksSinceFearDecay += 1;

                boolean shouldFearDecay = ticksSinceFearDecay >= plugin.utils.secondsToTicks(FEAR_DECAY_RATE);
                if (shouldFearDecay)
                    ticksSinceFearDecay = 0;

                for (Player player : Bukkit.getOnlinePlayers()) {

                    long mobsNearby = player.getNearbyEntities(10, 10, 10).stream()
                            .filter(e -> e instanceof Monster)
                            .count();

                    double playerFear = playerFearLevels.getOrDefault(player, 0.0);

                    if (mobsNearby > 0)
                        playerFear += mobsNearby * MOB_FEAR_MULTIPLIER;
                    if (plugin.bloodMoon.isActive && player.getLocation().getBlock().getLightFromSky() > 0)
                        playerFear += OUTSIDE_FEAR_LEVEL_INCREASE;
                    if (shouldFearDecay)
                        playerFear -= FEAR_DECAY_AMOUNT;

                    playerFear = Math.max(0.0, Math.min(playerFear, MAX_FEAR_LEVEL));
                    playerFearLevels.put(player, playerFear);

                    if (playerFear >= FEAR_THRESHOLD)
                        applyFearEffectsToPlayer(player);

                    updatePlayerFearBar(player, playerFear);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void applyFearEffectsToPlayer(Player player) {
        if (playersInFear.getOrDefault(player, false))
            return;

        playersInFear.put(player, true);
        int fearLastsFor = plugin.utils.secondsToTicks(FEAR_LASTS_FOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, fearLastsFor, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, fearLastsFor, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, fearLastsFor, 1));
        player.playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 1.0f);
        player.sendMessage("You feel a wave of fear wash over you...");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playersInFear.containsKey(player))
                    return;

                playersInFear.put(player, false);
            }
        }.runTaskLater(plugin, fearLastsFor);
    }

    private void updatePlayerFearBar(Player player, double playerFear) {
        double fearPercent = Math.min(playerFear / MAX_FEAR_LEVEL, 1.0);
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

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        playerFearLevels.put(event.getPlayer(), 0.0);
        playersInFear.put(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        playerFearLevels.remove(event.getPlayer());
        playersInFear.remove(event.getPlayer());
    }
}
