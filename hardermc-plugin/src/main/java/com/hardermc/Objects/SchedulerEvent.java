package com.hardermc.Objects;

import com.hardermc.HarderMC;
import com.hardermc.Systems.Scheduler.TimeOfDay;

public abstract class SchedulerEvent {
    protected final HarderMC plugin;

    public abstract int EVENT_INTERVAL();

    public abstract TimeOfDay EVENT_STARTS_AT();

    public abstract TimeOfDay EVENT_ENDS_AT();

    public abstract int EVENT_LASTS_FOR();

    public abstract String EVENT_ID();

    public int EVENT_DELAY_BEFORE_FIRST_START() {
        return 0;
    }

    protected SchedulerEvent(HarderMC plugin) {
        this.plugin = plugin;
    }

    protected void initialize() {
        plugin.scheduler.registerEvent(this);
    }

    public boolean isActive() {
        return plugin.scheduler.isEventActive(EVENT_ID());
    }

    public abstract void start();

    public abstract void end();

    public void onDayPassed() {
    }
}
