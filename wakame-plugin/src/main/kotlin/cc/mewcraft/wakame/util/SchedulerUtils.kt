package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.config.config
import cc.mewcraft.wakame.util.concurrent.ObservableLock
import cc.mewcraft.wakame.util.concurrent.lockAndRun
import cc.mewcraft.wakame.util.concurrent.tryLockAndRun
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.concurrent.*

val USE_NEKO_SCHEDULER: Boolean by config("performance", "neko_executor", "enabled") { boolean }

fun runTaskLater(delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(NEKO_PLUGIN, run, delay)

fun runTask(run: () -> Unit) =
    Bukkit.getScheduler().runTask(NEKO_PLUGIN, run)

fun runTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimer(NEKO_PLUGIN, run, delay, period)

fun runTaskSynchronized(lock: Any, run: () -> Unit) =
    Bukkit.getScheduler().runTask(NEKO_PLUGIN, Runnable { synchronized(lock, run) })

fun runTaskLaterSynchronized(lock: Any, delay: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(NEKO_PLUGIN, Runnable { synchronized(lock, run) }, delay)

fun runTaskTimerSynchronized(lock: Any, delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimer(NEKO_PLUGIN, Runnable { synchronized(lock, run) }, delay, period)

fun runSyncTaskWhenUnlocked(lock: ObservableLock, run: () -> Unit) {
    runTaskLater(1) { if (!lock.tryLockAndRun(run)) runSyncTaskWhenUnlocked(lock, run) }
}

fun runAsyncTask(run: () -> Unit) {
    if (USE_NEKO_SCHEDULER) AsyncExecutor.run(run)
    else Bukkit.getScheduler().runTaskAsynchronously(NEKO_PLUGIN, run)
}

fun runAsyncTaskLater(delay: Long, run: () -> Unit) {
    if (USE_NEKO_SCHEDULER) AsyncExecutor.runLater(delay * 50, run)
    else Bukkit.getScheduler().runTaskLaterAsynchronously(NEKO_PLUGIN, run, delay)
}

fun runAsyncTaskSynchronized(lock: Any, run: () -> Unit) {
    val task = { synchronized(lock, run) }
    if (USE_NEKO_SCHEDULER) AsyncExecutor.run(task)
    else Bukkit.getScheduler().runTaskAsynchronously(NEKO_PLUGIN, task)
}

fun runAsyncTaskWithLock(lock: ObservableLock, run: () -> Unit) {
    val task = { lock.lockAndRun(run) }
    if (USE_NEKO_SCHEDULER) AsyncExecutor.run(task)
    else Bukkit.getScheduler().runTaskAsynchronously(NEKO_PLUGIN, task)
}

fun runAsyncTaskTimerSynchronized(lock: Any, delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimerAsynchronously(NEKO_PLUGIN, Runnable { synchronized(lock, run) }, delay, period)

fun runAsyncTaskTimer(delay: Long, period: Long, run: () -> Unit) =
    Bukkit.getScheduler().runTaskTimerAsynchronously(NEKO_PLUGIN, run, delay, period)

internal object AsyncExecutor : KoinComponent {

    private val THREADS: Int by config("performance", "neko_executor", "threads") { int }
    private val LOGGER: Logger by inject()

    private lateinit var threadFactory: ThreadFactory
    private lateinit var executorService: ScheduledExecutorService

    init {
        if (USE_NEKO_SCHEDULER) {
            threadFactory = ThreadFactoryBuilder().setNameFormat("Async Wakame Worker - %d").build()
            executorService = ScheduledThreadPoolExecutor(THREADS, threadFactory)
            NEKO_PLUGIN.bind(executorService)
        }
    }

    fun run(task: () -> Unit): Future<*> =
        executorService.submit {
            try {
                task()
            } catch (t: Throwable) {
                LOGGER.error("An exception occurred running a task", t)
            }
        }

    fun runLater(delay: Long, task: () -> Unit): Future<*> =
        executorService.schedule({
            try {
                task()
            } catch (t: Throwable) {
                LOGGER.error("An exception occurred running a task", t)
            }
        }, delay, TimeUnit.MILLISECONDS)

}
