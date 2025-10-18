package com.hardermc;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.hardermc.Commands.bms;
import com.hardermc.Commands.rbms;
import com.hardermc.Effects.LightningStorm;
import com.hardermc.Effects.TNTRain;
import com.hardermc.Mechanics.BloodMoon;
import com.hardermc.Mechanics.PlayerFear;
import com.hardermc.Systems.LevelProgressor;
import com.hardermc.Systems.PlayerStatTracker;
import com.hardermc.Systems.StatMultiplier;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.Listener;

public class HarderMC extends JavaPlugin implements Listener {
  private static final Logger LOGGER = Logger.getLogger("hardermc");
  public StatMultiplier statMultiplier;
  public EventScheduler eventScheduler;
  public BloodMoon bloodMoon;
  public PlayerFear playerFear;
  public PlayerStatTracker playerStatTracker;
  public LevelProgressor levelProgressor;
  public LightningStorm lightningStorm;
  public TNTRain tntRain;
  public utils utils = new utils();

  public void onEnable() {
    LOGGER.info("HarderMC enabled");

    statMultiplier = new StatMultiplier();
    eventScheduler = new EventScheduler(this);
    bloodMoon = new BloodMoon(this);
    playerFear = new PlayerFear(this);
    playerStatTracker = new PlayerStatTracker(this);
    levelProgressor = new LevelProgressor(this);
    lightningStorm = new LightningStorm(this);
    tntRain = new TNTRain(this);

    getServer().getPluginManager().registerEvents(statMultiplier, this);
    getServer().getPluginManager().registerEvents(eventScheduler, this);
    getServer().getPluginManager().registerEvents(playerStatTracker, this);
    getServer().getPluginManager().registerEvents(levelProgressor, this);
    getServer().getPluginManager().registerEvents(playerFear, this);

    getCommand("bms").setExecutor(new bms(this));
    getCommand("rbms").setExecutor(new rbms(this));

    World world = Bukkit.getWorld("world");
    if (world != null)
      world.setDifficulty(Difficulty.HARD);
  }

  public void onDisable() {
    LOGGER.info("HarderMC disabled");
  }
}
