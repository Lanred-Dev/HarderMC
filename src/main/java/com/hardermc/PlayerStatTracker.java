package com.hardermc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerStatTracker implements Listener {
    private final HarderMC plugin;
    public final Map<Player, Integer> bloodMoonKills = new HashMap<>();
    public final Map<Player, Integer> bloodMoonDeaths = new HashMap<>();
    public final Map<Player, Integer> totalKills = new HashMap<>();
    public final Map<Player, Integer> totalDeaths = new HashMap<>();
    private final NamespacedKey KILLS_KEY;
    private final NamespacedKey DEATHS_KEY;

    public PlayerStatTracker(HarderMC plugin) {
        this.plugin = plugin;
        KILLS_KEY = new NamespacedKey(plugin, "kills");
        DEATHS_KEY = new NamespacedKey(plugin, "deaths");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.bloodMoon.isActive) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity.isDead() || !(entity instanceof LivingEntity victim)) {
            return;
        }

        Entity damager = event.getDamager();

        if (victim.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        if (entity instanceof Player player) {
            bloodMoonDeaths.put(player, bloodMoonDeaths.getOrDefault(player, 0) + 1);
            totalDeaths.put(player, totalDeaths.getOrDefault(player, 0) + 1);
        } else if (entity instanceof Monster && damager instanceof Player player) {
            bloodMoonKills.put(player, bloodMoonKills.getOrDefault(player, 0) + 1);
            totalKills.put(player, totalKills.getOrDefault(player, 0) + 1);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info(String.format("Player %s joined, loading stats", event.getPlayer().getName()));

        Player player = event.getPlayer();
        bloodMoonKills.putIfAbsent(player, 0);
        bloodMoonDeaths.putIfAbsent(player, 0);

        PersistentDataContainer data = player.getPersistentDataContainer();
        totalKills.put(player, data.getOrDefault(KILLS_KEY, PersistentDataType.INTEGER, 0));
        totalDeaths.put(player, data.getOrDefault(DEATHS_KEY, PersistentDataType.INTEGER, 0));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLogger().info(String.format("Player %s left, saving stats", event.getPlayer().getName()));

        Player player = event.getPlayer();
        bloodMoonKills.remove(player);
        bloodMoonDeaths.remove(player);

        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(KILLS_KEY, PersistentDataType.INTEGER, totalKills.getOrDefault(player, 0));
        data.set(DEATHS_KEY, PersistentDataType.INTEGER, totalDeaths.getOrDefault(player, 0));
        totalKills.remove(player);
        totalDeaths.remove(player);
    }

    public void displayThisBloodMoonStats() {
        plugin.getLogger().info("Displaying blood moon stats");

        for (Player player : Bukkit.getOnlinePlayers()) {
            int kills = bloodMoonKills.getOrDefault(player, 0);
            int deaths = bloodMoonDeaths.getOrDefault(player, 0);
            Bukkit.broadcastMessage(String.format("%s - %d kills, %d deaths", player.getName(), kills, deaths));
        }
    }
}
