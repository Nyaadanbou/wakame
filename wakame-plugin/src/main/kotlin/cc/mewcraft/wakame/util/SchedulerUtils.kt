package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO
import org.bukkit.Bukkit

fun runTaskLater(delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(NEKO, run, delay)

fun runTask(run: () -> Unit) =
    Bukkit.getScheduler().runTask(NEKO, run)

fun runTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimer(NEKO, run, delay, period)

fun runAsyncTask(run: () -> Unit) =
    Bukkit.getScheduler().runTaskAsynchronously(NEKO, run)

fun runAsyncTaskLater(delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLaterAsynchronously(NEKO, run, delay)

fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimerAsynchronously(NEKO, run, delay, period)
