package com.hardermc;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.hardermc.commands.bdloc;
import com.hardermc.commands.level;
import com.hardermc.commands.nextbm;
import com.hardermc.commands.stats;
import com.hardermc.events.BloodMoon;
import com.hardermc.events.BossDungeon;
import com.hardermc.services.Reward;
import com.hardermc.services.ServerData;
import com.hardermc.systems.common.Scheduler;
import com.hardermc.systems.game.Level;
import com.hardermc.systems.mob.MobHandler;
import com.hardermc.systems.player.PlayerHandler;
import com.hardermc.systems.player.PlayerStatTracker;
import com.hardermc.systems.player.stats.Fear;

public class HarderMC extends JavaPlugin {
  public static final Logger LOGGER = Logger.getLogger("HarderMC");
  public ServerData serverDataService;
  public Level levelSystem;
  public MobHandler mobHandler;
  public Scheduler scheduler;
  public BloodMoon bloodMoonEvent;
  public PlayerStatTracker playerStatTrackerSystem;
  public PlayerHandler playerHandlerSystem;
  public Fear fearSystem;
  public BossDungeon bossDungeonEvent;
  public Reward rewardService;

  public void onEnable() {
    LOGGER.info("HarderMC is enabled");

    serverDataService = new ServerData(this);
    scheduler = new Scheduler(this);
    bloodMoonEvent = new BloodMoon(this);
    mobHandler = new MobHandler(this);
    levelSystem = new Level(this);
    playerStatTrackerSystem = new PlayerStatTracker(this);
    playerHandlerSystem = new PlayerHandler();
    fearSystem = new Fear(this);
    bossDungeonEvent = new BossDungeon(this);
    rewardService = new Reward(this);

    PluginManager pluginManager = getServer().getPluginManager();
    pluginManager.registerEvents(bloodMoonEvent, this);
    pluginManager.registerEvents(mobHandler, this);
    pluginManager.registerEvents(playerStatTrackerSystem, this);
    pluginManager.registerEvents(playerHandlerSystem, this);
    pluginManager.registerEvents(levelSystem, this);
    pluginManager.registerEvents(fearSystem, this);
    pluginManager.registerEvents(bossDungeonEvent, this);
    pluginManager.registerEvents(rewardService, this);

    getCommand("nextbm").setExecutor(new nextbm(this));
    getCommand("bdloc").setExecutor(new bdloc(this));
    getCommand("level").setExecutor(new level(this));
    getCommand("stats").setExecutor(new stats(this));

    ensureDifficultyIsHard();
  }

  public void onDisable() {
    serverDataService.save();

    LOGGER.info("HarderMC is disabled");
  }

  private void ensureDifficultyIsHard() {
    World world = Bukkit.getWorld("world");

    if (world != null && world.getDifficulty() != Difficulty.HARD) {
      LOGGER.info("Resetting world difficulty to HARD");
      world.setDifficulty(Difficulty.HARD);
    }
  }
}
