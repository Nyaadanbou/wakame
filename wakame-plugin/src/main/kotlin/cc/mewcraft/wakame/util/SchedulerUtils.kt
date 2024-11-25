package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import org.bukkit.Bukkit

fun runTaskLater(delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(NEKO_PLUGIN, run, delay)

fun runTask(run: () -> Unit) =
    Bukkit.getScheduler().runTask(NEKO_PLUGIN, run)

fun runTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimer(NEKO_PLUGIN, run, delay, period)

fun runAsyncTask(run: () -> Unit) =
    Bukkit.getScheduler().runTaskAsynchronously(NEKO_PLUGIN, run)

fun runAsyncTaskLater(delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLaterAsynchronously(NEKO_PLUGIN, run, delay)

fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimerAsynchronously(NEKO_PLUGIN, run, delay, period)
