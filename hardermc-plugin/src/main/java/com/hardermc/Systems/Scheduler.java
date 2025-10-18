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
    private final List<String> currentEvents = new ArrayList<>();
    private final Map<String, Integer> eventTimers = new HashMap<>();
    private final List<SchedulerEvent> scheduledEvents = new ArrayList<>();
    public TimeOfDay currentTimeOfDay;
    private TimeOfDay lastTimeOfDay;

    public static enum TimeOfDay {
        NIGHT,
        DAY,
    }

    public Scheduler(HarderMC plugin) {
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
                                eventTimers.put(eventID, daysUntilEvent - 1);
                            }
                        } else if (currentTimeOfDay == event.EVENT_ENDS_AT() && isActive) {
                            restartEvent(event);
                        }
                    }
                }

                lastTimeOfDay = currentTimeOfDay;
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    public boolean isEventActive(String eventID) {
        return currentEvents.contains(eventID);
    }

    public int getDaysUntilEvent(String eventID) {
        return eventTimers.getOrDefault(eventID, -1);
    }

    public void registerEvent(SchedulerEvent event) {
        eventTimers.put(event.EVENT_ID(), event.EVENT_INTERVAL());
        scheduledEvents.add(event);
        HarderMC.LOGGER.info(String.format("Registered scheduler event: %s", event.EVENT_ID()));
    }

    private void restartEvent(SchedulerEvent event) {
        event.end();
        currentEvents.remove(event.EVENT_ID());
        eventTimers.put(event.EVENT_ID(), event.EVENT_INTERVAL());
        HarderMC.LOGGER.info(String.format("Event %s restarted", event.EVENT_ID()));
    }

    private void startEvent(SchedulerEvent event) {
        event.start();
        currentEvents.add(event.EVENT_ID());
        HarderMC.LOGGER.info(String.format("Event %s started", event.EVENT_ID()));
    }
}
