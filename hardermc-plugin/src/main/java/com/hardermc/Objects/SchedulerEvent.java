package com.hardermc.Objects;

import com.hardermc.HarderMC;
import com.hardermc.Systems.Scheduler.TimeOfDay;

public abstract class SchedulerEvent {
    protected final HarderMC plugin;

    public abstract Integer EVENT_INTERVAL();

    public abstract TimeOfDay EVENT_STARTS_AT();

    public abstract TimeOfDay EVENT_ENDS_AT();

    public abstract String EVENT_ID();

    protected SchedulerEvent(HarderMC plugin) {
        this.plugin = plugin;
        plugin.scheduler.registerEvent(this);
    }

    public boolean isActive() {
        return plugin.scheduler.isEventActive(EVENT_ID());
    }

    public abstract void start();

    public abstract void end();
}
