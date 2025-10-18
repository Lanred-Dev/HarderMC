package com.hardermc.Systems;

import java.util.HashMap;
import java.util.Map;

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

import com.hardermc.HarderMC;

public class PlayerStatTracker implements Listener {
    public final Map<Player, Integer> kills = new HashMap<>();
    public final Map<Player, Integer> deaths = new HashMap<>();
    private final NamespacedKey KILLS_KEY;
    private final NamespacedKey DEATHS_KEY;

    public PlayerStatTracker(HarderMC plugin) {
        this.KILLS_KEY = new NamespacedKey(plugin, "KILLS");
        this.DEATHS_KEY = new NamespacedKey(plugin, "DEATHS");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HarderMC.LOGGER.info(String.format("Loading stats for %s", event.getPlayer().getName()));

        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();
        kills.put(player, data.getOrDefault(KILLS_KEY, PersistentDataType.INTEGER, 0));
        deaths.put(player, data.getOrDefault(DEATHS_KEY, PersistentDataType.INTEGER, 0));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        HarderMC.LOGGER.info(String.format("Saving stats for %s", event.getPlayer().getName()));

        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(KILLS_KEY, PersistentDataType.INTEGER, kills.getOrDefault(player, 0));
        data.set(DEATHS_KEY, PersistentDataType.INTEGER, deaths.getOrDefault(player, 0));
        kills.remove(player);
        deaths.remove(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity.isDead() || !(entity instanceof LivingEntity victim)) {
            return;
        }

        Entity damager = event.getDamager();

        if (victim.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        if (entity instanceof Player player) {
            deaths.put(player, deaths.getOrDefault(player, 0) + 1);
        } else if (entity instanceof Monster && damager instanceof Player player) {
            kills.put(player, kills.getOrDefault(player, 0) + 1);
        }
    }
}