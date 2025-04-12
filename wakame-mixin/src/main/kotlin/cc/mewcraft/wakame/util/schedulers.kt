@file:JvmName("SchedulerUtils")

package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.PluginHolder
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask

/**
 * Shortcut for [BukkitScheduler.runTask], registered under the Koish plugin.
 */
fun runTask(run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTask(PluginHolder.INSTANCE, run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLater], registered under the Koish plugin.
 */
fun runTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLater(PluginHolder.INSTANCE, run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimer], registered under the Koish plugin.
 */
fun runTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskTimer(PluginHolder.INSTANCE, run, delay, period)
}

/**
 * Shortcut for [BukkitScheduler.runTaskAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTask(run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskAsynchronously(PluginHolder.INSTANCE, run)
}

/**
 * Shortcut for [BukkitScheduler.runTaskLaterAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskLater(delay: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLaterAsynchronously(PluginHolder.INSTANCE, run, delay)
}

/**
 * Shortcut for [BukkitScheduler.runTaskTimerAsynchronously], registered under the Koish plugin.
 */
fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskTimerAsynchronously(PluginHolder.INSTANCE, run, delay, period)
}
