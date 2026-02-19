package cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.identifier.ActionIdentifier
import org.betonquest.betonquest.api.identifier.ScheduleIdentifier
import org.betonquest.betonquest.api.instruction.section.SectionInstruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.schedule.CatchupStrategy
import org.betonquest.betonquest.api.schedule.Schedule
import org.betonquest.betonquest.api.schedule.Scheduler
import org.betonquest.betonquest.api.service.action.ActionManager
import org.betonquest.betonquest.schedule.impl.BaseScheduleFactory
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * A schedule that runs actions every N Minecraft game ticks (periodic execution).
 *
 * Semantic: An interval of N means the actions will execute every N ticks.
 * - interval = 1: execute every tick (1, 2, 3, 4...)
 * - interval = 20: execute every 20 ticks (1, 21, 41, 61...)
 *
 * @param scheduleID the unique identifier for this schedule
 * @param actions the list of action IDs to execute at each scheduled interval
 * @param catchup the catchup strategy to use if ticks are missed
 * @param intervalTicks how many ticks between each execution (the execution period)
 */
class GameTickSchedule(
    scheduleID: ScheduleIdentifier,
    actions: List<ActionIdentifier>,
    catchup: CatchupStrategy,
    val intervalTicks: Int,
) : Schedule(scheduleID, actions, catchup)

/**
 * Factory to create [GameTickSchedule] instances.
 *
 * Time format: a positive integer string representing the execution period in Minecraft ticks.
 * - time: "1" means execute every tick (1, 2, 3, 4...)
 * - time: "20" means execute every 20 ticks (1, 21, 41, 61...)
 * - time: "100" means execute every 100 ticks (1, 101, 201, 301...)
 */
class GameTickScheduleFactory : BaseScheduleFactory<GameTickSchedule>() {

    @Throws(QuestException::class)
    override fun createNewInstance(scheduleID: ScheduleIdentifier, config: SectionInstruction): GameTickSchedule {
        val scheduleData = parseScheduleData(config)
        val rawTime = scheduleData.time().trim()
        val interval = rawTime.toIntOrNull() ?: throw QuestException("Unable to parse time '$rawTime' as tick interval")
        require(interval > 0) { "Time must be a positive integer tick interval, got '$rawTime'" }
        return GameTickSchedule(scheduleID, scheduleData.actions(), scheduleData.catchup(), interval)
    }
}

/**
 * Scheduler that runs [GameTickSchedule] instances based on the Minecraft server tick loop.
 *
 * This scheduler implements PERIODIC scheduling: a schedule with interval=N will execute
 * every N ticks.
 *
 * Semantics:
 * - interval = 1: execute EVERY tick (1, 2, 3, 4, 5...)
 * - interval = 2: execute EVERY 2 ticks (1, 3, 5, 7, 9...)
 * - interval = 20: execute EVERY 20 ticks (1, 21, 41, 61...)
 *
 * The scheduler starts at the current server tick provided to start(now). The first execution
 * happens at that tick, and then every interval ticks thereafter.
 *
 * Implementation uses ServerTickEndEvent:
 * - Fires AFTER all server tick logic (entity movement, block updates, etc.)
 * - Ensures scheduled actions don't interfere with server-side calculations
 * - Provides the cleanest execution point for time-sensitive actions
 *
 * No catchup logic: if the server pauses, goes offline, or ticks are skipped, any
 * missed executions are simply not replayed later.
 */
class GameTickScheduler(
    private val log: BetonQuestLogger,
    private val actionManager: ActionManager,
    private val plugin: JavaPlugin,
) : Scheduler<GameTickSchedule, Int>(log, actionManager), Listener {

    /**
     * Next server tick at which each schedule should be executed.
     *
     * This is initialized when the scheduler is started at the current server tick,
     * and then advanced by the full interval after each execution.
     *
     * IMPORTANT: This tracks the NEXT execution tick, not a counter.
     *
     * The schedule uses periodic execution based on interval:
     * - interval = 1: execute every tick (ticks 1, 2, 3, 4...)
     * - interval = 2: execute every 2 ticks (ticks 1, 3, 5, 7...)
     * - interval = 20: execute every 20 ticks (ticks 1, 21, 41, 61...)
     *
     * Algorithm: We track when the scheduler was started and use modulo arithmetic to
     * determine execution points. For a schedule started at tick `S` with interval `I`:
     *   - Execute when: (tick - S) % I == 0
     *   - This ensures execution at S, S+I, S+2I, S+3I...
     *
     * We maintain nextExecutionTick to avoid recalculating modulo every tick:
     *   - After executing at tick T, set nextExecutionTick = T + I
     *
     * No catchup is performed: if ticks are skipped or the server is offline, missed
     * executions are ignored.
     */
    private val nextExecutionTick: HashMap<GameTickSchedule, Int> = HashMap()

    /**
     * Start the game tick scheduler.
     *
     * For each registered [GameTickSchedule], this method calculates the first server
     * tick at which it should run, starting from the supplied `now` tick. Subsequent
     * executions are spaced exactly [GameTickSchedule.intervalTicks] ticks apart.
     * No previously missed executions are replayed.
     *
     * @param now the server tick to use as the logical start time; callers should normally pass
     * [Bukkit.getCurrentTick] here
     */
    override fun start(now: Int) {
        log.debug("Starting game tick scheduler.")

        nextExecutionTick.clear()
        for (schedule in schedules.values) {
            val interval = schedule.intervalTicks
            if (interval <= 0) continue
            nextExecutionTick[schedule] = now
        }

        Bukkit.getPluginManager().registerEvents(this, plugin)

        super.start(now)
        log.debug("Game tick scheduler start complete.")
    }

    /**
     * Called at the END of every server tick, after all server logic has completed.
     *
     * This method checks each registered [GameTickSchedule] to see whether the current
     * server tick has reached or passed the next scheduled execution tick. If so, the schedule
     * is executed once and its next execution tick is advanced by the configured interval.
     *
     * SCHEDULING SEMANTICS: "Every N ticks"
     *
     * The schedule is PERIODIC with a period equal to the interval:
     * - interval = 1: execute EVERY tick (1, 2, 3, 4, 5...)
     * - interval = 2: execute EVERY 2 ticks (1, 3, 5, 7, 9...)
     * - interval = 20: execute EVERY 20 ticks (1, 21, 41, 61...)
     *
     * Note: A tick has a DURATION. Tick N includes the time from the end of Tick N-1 to
     * the end of Tick N. An interval of 20 means "every 20 ticks", which is 20 ticks of duration.
     *
     * Example with interval=20, starting at tick 1:
     *   - Tick 1: (1 - 1) % 20 = 0 → EXECUTE (1st execution)
     *   - Tick 2-20: (N - 1) % 20 ≠ 0 → skip
     *   - Tick 21: (21 - 1) % 20 = 0 → EXECUTE (2nd execution, 20 ticks after 1st)
     *   - Tick 22-40: (N - 1) % 20 ≠ 0 → skip
     *   - Tick 41: (41 - 1) % 20 = 0 → EXECUTE (3rd execution, 20 ticks after 2nd)
     *
     * Implementation: We use nextExecutionTick to avoid recalculating modulo every tick.
     * When tick >= nextExecutionTick:
     *   1. Execute the schedule
     *   2. Set nextExecutionTick = current_tick + interval
     *
     * This naturally handles the "every N ticks" semantic without special cases.
     */
    @EventHandler
    private fun on(event: ServerTickEndEvent) {
        if (!isRunning) return

        val tick = event.tickNumber
        for (schedule in schedules.values) {
            val interval = schedule.intervalTicks
            if (interval <= 0) continue
            val nextTick = nextExecutionTick[schedule] ?: continue
            if (tick >= nextTick) {
                executeOnce(schedule)
                nextExecutionTick[schedule] = nextTick + interval
            }
        }
    }

    /**
     * Execute the given schedule once and record its execution time in the cache.
     *
     * @param schedule the schedule to execute
     */
    private fun executeOnce(schedule: GameTickSchedule) {
        executeActions(schedule)
    }

    /**
     * Get the current scheduler time.
     *
     * For game tick based schedules this is simply the current server tick provided by
     * Paper/Bukkit.
     *
     * @return the current server tick
     */
    override fun getNow(): Int {
        return Bukkit.getCurrentTick()
    }

    /**
     * Stop the game tick scheduler and unregister the tick listener.
     */
    override fun stop() {
        if (isRunning) {
            HandlerList.unregisterAll(this)
            nextExecutionTick.clear()
            super.stop()
            log.debug("Stopped game tick scheduler.")
        }
    }
}
