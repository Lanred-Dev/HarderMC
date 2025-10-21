package com.hardermc.Systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.hardermc.HarderMC;
import com.hardermc.Objects.SchedulerEvent;

public class Scheduler {
    public static final int NIGHT_START_TIME = 13000;
    public static final int NIGHT_END_TIME = 23000;
    private final Map<String, Integer> currentEvents = new HashMap<>();
    private final Map<String, Integer> eventTimers = new HashMap<>();
    private final List<SchedulerEvent> scheduledEvents = new ArrayList<>();
    public TimeOfDay currentTimeOfDay;
    private TimeOfDay lastTimeOfDay;
    private final HarderMC plugin;

    public static enum TimeOfDay {
        NIGHT,
        DAY,
    }

    public Scheduler(HarderMC plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld("world");

                if (world == null)
                    return;

                long time = world.getTime();
                currentTimeOfDay = (time >= Scheduler.NIGHT_START_TIME && time <= Scheduler.NIGHT_END_TIME)
                        ? TimeOfDay.NIGHT
                        : TimeOfDay.DAY;

                if (lastTimeOfDay != currentTimeOfDay) {
                    for (SchedulerEvent event : scheduledEvents) {
                        boolean isActive = isEventActive(event.EVENT_ID());
                        String eventID = event.EVENT_ID();

                        if (currentTimeOfDay == event.EVENT_STARTS_AT() && !isActive) {
                            Integer daysUntilEvent = eventTimers.getOrDefault(eventID, event.EVENT_INTERVAL());

                            if (daysUntilEvent <= 0) {
                                startEvent(event);
                            } else {
                                daysUntilEvent--;
                                eventTimers.put(eventID, daysUntilEvent);
                                plugin.serverDataService.set(eventID, daysUntilEvent);
                            }
                        } else if (currentTimeOfDay == event.EVENT_ENDS_AT() && isActive
                                && currentEvents.get(eventID) <= 0) {
                            endEvent(event);
                        } else if (isActive && currentTimeOfDay == event.EVENT_ENDS_AT()) {
                            int daysLeft = currentEvents.get(eventID) - 1;

                            if (daysLeft > 0) {
                                event.onDayPassed();
                                currentEvents.put(eventID, daysLeft);
                                plugin.serverDataService.set(eventID, -daysLeft);
                            }
                        }
                    }
                }

                lastTimeOfDay = currentTimeOfDay;
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    public boolean isEventActive(String eventID) {
        return currentEvents.containsKey(eventID);
    }

    public int getDaysUntilEvent(String eventID) {
        return eventTimers.getOrDefault(eventID, -1);
    }

    public int getDaysLeftInEvent(String eventID) {
        return currentEvents.getOrDefault(eventID, -1);
    }

    public void registerEvent(SchedulerEvent event) {
        if (plugin.serverDataService.has(event.EVENT_ID())) {
            double doubleValue = (double) plugin.serverDataService.get(event.EVENT_ID(), 0.0);
            int value = (int) doubleValue;

            if (value < 0) {
                value = Math.abs(value);
                event.start();
                currentEvents.put(event.EVENT_ID(), value);
                eventTimers.put(event.EVENT_ID(), 0);
                HarderMC.LOGGER
                        .info(String.format("Event %s started because it was previously running", event.EVENT_ID()));
            } else {
                eventTimers.put(event.EVENT_ID(), value);
            }
        } else {
            eventTimers.put(event.EVENT_ID(), event.EVENT_INTERVAL() + event.EVENT_DELAY_BEFORE_FIRST_START());
        }

        scheduledEvents.add(event);
        HarderMC.LOGGER.info(String.format("Registered %s with the scheduler", event.EVENT_ID()));
    }

    private void endEvent(SchedulerEvent event) {
        event.end();
        currentEvents.remove(event.EVENT_ID());
        eventTimers.put(event.EVENT_ID(), event.EVENT_INTERVAL());
        plugin.serverDataService.set(event.EVENT_ID(), event.EVENT_INTERVAL());
        HarderMC.LOGGER.info(String.format("Event %s ended", event.EVENT_ID()));
    }

    private void startEvent(SchedulerEvent event) {
        event.start();
        currentEvents.put(event.EVENT_ID(), event.EVENT_LASTS_FOR());
        plugin.serverDataService.set(event.EVENT_ID(), -event.EVENT_LASTS_FOR());
        HarderMC.LOGGER.info(String.format("Event %s started", event.EVENT_ID()));
    }
}
