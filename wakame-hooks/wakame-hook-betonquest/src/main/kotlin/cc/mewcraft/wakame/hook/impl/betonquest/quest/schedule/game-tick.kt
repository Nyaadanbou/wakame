package cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.config.quest.QuestPackageManager
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.quest.QuestTypeApi
import org.betonquest.betonquest.api.quest.action.ActionID
import org.betonquest.betonquest.api.schedule.CatchupStrategy
import org.betonquest.betonquest.api.schedule.Schedule
import org.betonquest.betonquest.api.schedule.ScheduleID
import org.betonquest.betonquest.api.schedule.Scheduler
import org.betonquest.betonquest.kernel.processor.quest.PlaceholderProcessor
import org.betonquest.betonquest.schedule.impl.BaseScheduleFactory
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * A schedule that runs actions every N Minecraft game ticks.
 *
 * @param scheduleID the unique identifier for this schedule
 * @param actions the list of action IDs to execute on each tick interval
 * @param catchup the catchup strategy to use if ticks are missed
 * @param intervalTicks the interval in Minecraft ticks between executions
 */
class GameTickSchedule(
    scheduleID: ScheduleID,
    actions: List<ActionID>,
    catchup: CatchupStrategy,
    val intervalTicks: Int,
) : Schedule(scheduleID, actions, catchup)

/**
 * Factory to create [GameTickSchedule] instances.
 *
 * Time format: a positive integer string representing the interval in Minecraft ticks.
 * For example, time: "10" means the schedule runs every 10 ticks.
 */
class GameTickScheduleFactory(
    variableProcessor: PlaceholderProcessor,
    packManager: QuestPackageManager,
) : BaseScheduleFactory<GameTickSchedule>(variableProcessor, packManager) {

    @Throws(QuestException::class)
    override fun createNewInstance(scheduleID: ScheduleID, config: ConfigurationSection): GameTickSchedule {
        val scheduleData = parseScheduleData(scheduleID.getPackage(), config)
        val rawTime = scheduleData.time().trim()
        val interval = rawTime.toIntOrNull() ?: throw QuestException("Unable to parse time '$rawTime' as tick interval")
        require(interval > 0) { "Time must be a positive integer tick interval, got '$rawTime'" }
        return GameTickSchedule(scheduleID, scheduleData.actions(), scheduleData.catchup(), interval)
    }
}

/**
 * Scheduler that runs [GameTickSchedule] instances based on the Minecraft server tick loop.
 *
 * This scheduler uses the Paper [ServerTickStartEvent]
 * to track the current server tick and maintains, for each schedule, the next tick at which it
 * should be executed. Each schedule therefore runs from its own start time every
 * [GameTickSchedule.intervalTicks] interval ticks. There is intentionally no catchup
 * logic: if the server is paused, offline, or ticks are skipped, any missed executions are not
 * replayed later.
 */
class GameTickScheduler(
    private val log: BetonQuestLogger,
    private val questTypeApi: QuestTypeApi,
    private val plugin: JavaPlugin,
) : Scheduler<GameTickSchedule, Int>(log, questTypeApi), Listener {

    /**
     * Next server tick at which each schedule should be executed.
     *
     * This is initialized when the scheduler is started, based on the current
     * server tick plus the schedule's interval, and then advanced by the
     * interval after each execution. No catchup is performed: if ticks are
     * skipped or the server is offline, missed executions are ignored.
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
        val startTick = now

        // For each schedule, the first execution is scheduled at startTick itself.
        // Subsequent executions are spaced by exactly 'interval' ticks based on that point.
        for (schedule in schedules.values) {
            val interval = schedule.intervalTicks
            if (interval <= 0) continue
            nextExecutionTick[schedule] = startTick
        }

        Bukkit.getPluginManager().registerEvents(this, plugin)

        super.start(now)
        log.debug("Game tick scheduler start complete.")
    }

    /**
     * Called at the start of every server tick.
     *
     * This method checks each registered [GameTickSchedule] to see whether the current
     * server tick has reached or passed the next scheduled execution tick. If so, the schedule
     * is executed once and its next execution tick is advanced by its configured interval.
     */
    @EventHandler
    private fun on(event: ServerTickStartEvent) {
        if (!isRunning) return

        val tick = Bukkit.getCurrentTick()
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
