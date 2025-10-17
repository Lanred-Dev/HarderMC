package com.hardermc;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.hardermc.Commands.bms;
import com.hardermc.Commands.rbms;

import org.bukkit.event.Listener;

public class HarderMC extends JavaPlugin implements Listener {
  private static final Logger LOGGER = Logger.getLogger("hardermc");
  public StatMultiplier statMultiplier;
  public EventScheduler eventScheduler;
  public BloodMoonHandler bloodMoon;
  public PlayerStatTracker playerStatTracker;
  public LevelProgressor levelProgressor;

  public void onEnable() {
    LOGGER.info("HarderMC enabled");

    statMultiplier = new StatMultiplier();
    eventScheduler = new EventScheduler(this);
    bloodMoon = new BloodMoonHandler(this);
    playerStatTracker = new PlayerStatTracker(this);
    levelProgressor = new LevelProgressor(this);

    getServer().getPluginManager().registerEvents(statMultiplier, this);
    getServer().getPluginManager().registerEvents(eventScheduler, this);
    getServer().getPluginManager().registerEvents(playerStatTracker, this);
    getServer().getPluginManager().registerEvents(levelProgressor, this);

    getCommand("bms").setExecutor(new bms(this));
    getCommand("rbms").setExecutor(new rbms(this));
  }

  public void onDisable() {
    LOGGER.info("HarderMC disabled");
  }
}
