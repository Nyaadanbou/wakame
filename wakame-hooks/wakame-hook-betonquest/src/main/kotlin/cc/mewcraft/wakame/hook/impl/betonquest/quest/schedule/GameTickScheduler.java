package cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.quest.QuestTypeApi;
import org.betonquest.betonquest.api.schedule.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Scheduler that runs {@link GameTickSchedule} instances based on the Minecraft server tick loop.
 *
 * <p>This scheduler uses the Paper {@link ServerTickStartEvent}
 * to track the current server tick and maintains, for each schedule, the next tick at which it
 * should be executed. Each schedule therefore runs from its own start time every
 * {@link GameTickSchedule#getInterval() interval ticks}. There is intentionally no catchup
 * logic: if the server is paused, offline, or ticks are skipped, any missed executions are not
 * replayed later.</p>
 */
public class GameTickScheduler extends Scheduler<GameTickSchedule, Integer> implements Listener {

    /**
     * Logger used to write scheduler related log messages.
     */
    private final BetonQuestLogger log;

    /**
     * The Bukkit plugin instance used to register and unregister this scheduler as a listener.
     */
    private final JavaPlugin plugin;

    /**
     * Next server tick at which each schedule should be executed.
     *
     * <p>This is initialized when the scheduler is started, based on the current
     * server tick plus the schedule's interval, and then advanced by the
     * interval after each execution. No catchup is performed: if ticks are
     * skipped or the server is offline, missed executions are ignored.</p>
     */
    private final Map<GameTickSchedule, Integer> nextExecutionTick = new HashMap<>();

    /**
     * Create a new game tick scheduler using the global {@link BetonQuest} plugin instance.
     *
     * @param log          the logger that will be used for logging
     * @param questTypeApi the API used to execute quest events
     */
    public GameTickScheduler(final BetonQuestLogger log, final QuestTypeApi questTypeApi) {
        this(log, questTypeApi, BetonQuest.getInstance());
    }

    /**
     * Create a new game tick scheduler with an explicit plugin instance.
     *
     * <p>This constructor is primarily useful for tests where a custom or mocked
     * {@link JavaPlugin} is required.</p>
     *
     * @param log          the logger that will be used for logging
     * @param questTypeApi the API used to execute quest events
     * @param plugin       the Bukkit plugin instance used to register listeners
     */
    public GameTickScheduler(final BetonQuestLogger log, final QuestTypeApi questTypeApi, final JavaPlugin plugin) {
        super(log, questTypeApi);
        this.log = log;
        this.plugin = plugin;
    }

    /**
     * Start the game tick scheduler.
     *
     * <p>For each registered {@link GameTickSchedule}, this method calculates the first server
     * tick at which it should run, starting from the supplied {@code now} tick. Subsequent
     * executions are spaced exactly {@link GameTickSchedule#getInterval()} ticks apart.
     * No previously missed executions are replayed.</p>
     *
     * @param now the server tick to use as the logical start time; callers should normally pass
     *            {@link Bukkit#getCurrentTick()} here
     */
    @Override
    public void start(final Integer now) {
        log.debug("Starting game tick scheduler.");

        nextExecutionTick.clear();
        final int startTick = now;

        // For each schedule, the first execution is scheduled at (startTick + interval).
        // Subsequent executions are spaced by exactly 'interval' ticks based on that point.
        for (final GameTickSchedule schedule : schedules.values()) {
            final int interval = schedule.getInterval();
            if (interval <= 0) {
                continue;
            }
            nextExecutionTick.put(schedule, startTick + interval);
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);

        super.start(now);
        log.debug("Game tick scheduler start complete.");
    }

    /**
     * Called at the start of every server tick.
     *
     * <p>This method checks each registered {@link GameTickSchedule} to see whether the current
     * server tick has reached or passed the next scheduled execution tick. If so, the schedule
     * is executed once and its next execution tick is advanced by its configured interval.</p>
     *
     * @param event the server tick start event fired by Paper
     */
    @EventHandler
    private void on(final ServerTickStartEvent event) {
        if (!isRunning()) return;

        final int tick = Bukkit.getCurrentTick();
        for (final GameTickSchedule schedule : schedules.values()) {
            final int interval = schedule.getInterval();
            if (interval <= 0) {
                continue;
            }
            final Integer nextTick = nextExecutionTick.get(schedule);

            // If the schedule was added after start(), initialize its next execution relative to the current tick.
            final int effectiveNextTick = nextTick == null ? tick + interval : nextTick;
            if (tick < effectiveNextTick) {
                nextExecutionTick.put(schedule, effectiveNextTick);
                continue;
            }

            // Run once when the current tick reaches or passes the scheduled tick,
            // then schedule the next execution interval ticks later. If we skipped
            // multiple ticks, we still only execute once and advance from the
            // current tick, effectively dropping missed executions.
            executeOnce(schedule);
            nextExecutionTick.put(schedule, tick + interval);
        }
    }

    /**
     * Execute the given schedule once and record its execution time in the cache.
     *
     * @param schedule the schedule to execute
     */
    private void executeOnce(final GameTickSchedule schedule) {
        executeEvents(schedule);
    }

    /**
     * Get the current scheduler time.
     *
     * <p>For game tick based schedules this is simply the current server tick provided by
     * Paper/Bukkit.</p>
     *
     * @return the current server tick
     */
    @Override
    protected Integer getNow() {
        // Use the server's current tick directly; no separate counter is required.
        return Bukkit.getCurrentTick();
    }

    /**
     * Stop the game tick scheduler and unregister the tick listener.
     */
    @Override
    public void stop() {
        if (isRunning()) {
            HandlerList.unregisterAll(this);
            nextExecutionTick.clear();
            super.stop();
            log.debug("Stopped game tick scheduler.".toLowerCase(Locale.ROOT));
        }
    }
}
