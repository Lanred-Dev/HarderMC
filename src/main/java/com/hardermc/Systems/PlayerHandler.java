package com.hardermc.Systems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hardermc.Objects.MultiplierGroup;

public class PlayerHandler implements Listener {
    private static final double MAX_HUNGER_RATE_MULTIPLIER = 3.0;
    public final Map<Player, MultiplierGroup> hungerRateMultipliers = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        hungerRateMultipliers.put(event.getPlayer(), new MultiplierGroup(MAX_HUNGER_RATE_MULTIPLIER));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        hungerRateMultipliers.remove(event.getPlayer());
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        double multiplier = hungerRateMultipliers.get(player).getTotal();
        event.setFoodLevel((int) Math.max(event.getFoodLevel() - (player.getExhaustion() * multiplier), 0));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDroppedExp(0);
    }
}
