package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.PLUGIN_READY
import kotlinx.coroutines.SupervisorJob
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask

/**
 * Shortcut for [BukkitScheduler.runTask], registered under the Koish plugin.
 */
fun runTask(run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTask(Koish, run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLater], registered under the Koish plugin.
 */
fun runTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTaskLater(Koish, run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimer], registered under the Koish plugin.
 */
fun runTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTaskTimer(Koish, run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTask(run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTaskAsynchronously(Koish, run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLaterAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTaskLaterAsynchronously(Koish, run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimerAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    checkSchedulerAvailability()
    return Bukkit.getScheduler().runTaskTimerAsynchronously(Koish, run, delay, period)
}

private fun checkSchedulerAvailability() {
    check(PLUGIN_READY) { "Scheduler cannot be used this early! Use a post-world initialization stage for this." }
}

internal object AsyncExecutor {

    val SUPERVISOR = SupervisorJob()

}
