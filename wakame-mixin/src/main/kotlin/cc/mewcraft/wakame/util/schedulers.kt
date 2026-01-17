@file:JvmName("SchedulerUtils")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.PluginProvider
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Shortcut for [BukkitScheduler.runTask], registered under the Koish plugin.
 */
fun runTask(run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTask(PluginProvider.get(), run)
}

/**
 * Shortcut for [BukkitScheduler.runTask], registered under the Koish plugin.
 */
fun runTask(run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTask(PluginProvider.get(), run)
}

/**
 * Shortcut for [BukkitScheduler.runTask] but count aware, registered under the Koish plugin.
 */
fun runTask(run: (task: BukkitTask, count: Long) -> Unit) {
    runTask(countAwareTask(run))
}

/**
 * Shortcut for [BukkitScheduler.runTaskLater], registered under the Koish plugin.
 */
fun runTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLater(PluginProvider.get(), run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLater], registered under the Koish plugin.
 */
fun runTaskLater(delay: Long, run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTaskLater(PluginProvider.get(), run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLater] but count aware, registered under the Koish plugin.
 */
fun runTaskLater(delay: Long, run: (task: BukkitTask, count: Long) -> Unit) {
    runTaskLater(delay, countAwareTask(run))
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimer], registered under the Koish plugin.
 */
fun runTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskTimer(PluginProvider.get(), run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimer], registered under the Koish plugin.
 */
fun runTaskTimer(delay: Long, period: Long, run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTaskTimer(PluginProvider.get(), run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimer] but count aware, registered under the Koish plugin.
 */
fun runTaskTimer(delay: Long, period: Long, run: (task: BukkitTask, count: Long) -> Unit) {
    runTaskTimer(delay, period, countAwareTask(run))
}

/**
 * Shortcut for [BukkitScheduler.runTaskAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTask(run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskAsynchronously(PluginProvider.get(), run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTask(run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(PluginProvider.get(), run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskAsynchronously] but count aware, registered under the Koish plugin.
 */
fun runAsyncTask(run: (task: BukkitTask, count: Long) -> Unit) {
    runAsyncTask(countAwareTask(run))
}

/**
 * Shortcut for [BukkitScheduler.runTaskLaterAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLaterAsynchronously(PluginProvider.get(), run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLaterAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskLater(delay: Long, run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTaskLaterAsynchronously(PluginProvider.get(), run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLaterAsynchronously] but count aware, registered under the Koish plugin.
 */
fun runAsyncTaskLater(delay: Long, run: (task: BukkitTask, count: Long) -> Unit) {
    runAsyncTaskLater(delay, countAwareTask(run))
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimerAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskTimerAsynchronously(PluginProvider.get(), run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimerAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskTimer(delay: Long, period: Long, run: (task: BukkitTask) -> Unit) {
    Bukkit.getScheduler().runTaskTimerAsynchronously(PluginProvider.get(), run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimerAsynchronously] but count aware, registered under the Koish plugin.
 */
fun runAsyncTaskTimer(delay: Long, period: Long, run: (task: BukkitTask, count: Long) -> Unit) {
    runAsyncTaskTimer(delay, period, countAwareTask(run))
}

/**
 * Creates a count aware task.
 */
@OptIn(ExperimentalAtomicApi::class)
private fun countAwareTask(run: (BukkitTask, Long) -> Unit): (task: BukkitTask) -> Unit {
    val count = AtomicLong(0)
    val wrapper = fun(task: BukkitTask) {
        run(task, count.fetchAndAdd(1))
    }
    return wrapper
}
