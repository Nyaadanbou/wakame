package cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule;

import org.betonquest.betonquest.api.quest.event.EventID;
import org.betonquest.betonquest.api.schedule.CatchupStrategy;
import org.betonquest.betonquest.api.schedule.Schedule;
import org.betonquest.betonquest.api.schedule.ScheduleID;

import java.util.List;

/**
 * A schedule that runs events every N Minecraft game ticks.
 */
public class GameTickSchedule extends Schedule {

    /**
     * Interval in Minecraft ticks between executions.
     */
    private final int interval;

    /**
     * Creates a new game tick based schedule.
     *
     * @param scheduleID    the schedule id
     * @param events        the events to execute
     * @param catchup       the catchup strategy
     * @param interval interval between executions in ticks, must be > 0
     */
    public GameTickSchedule(final ScheduleID scheduleID, final List<EventID> events,
                            final CatchupStrategy catchup, final int interval) {
        super(scheduleID, events, catchup);
        this.interval = interval;
    }

    /**
     * Get the interval between executions in Minecraft ticks.
     *
     * @return interval in ticks
     */
    public int getInterval() {
        return interval;
    }
}
