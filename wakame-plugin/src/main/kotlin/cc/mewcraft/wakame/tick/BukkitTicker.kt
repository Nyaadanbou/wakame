package cc.mewcraft.wakame.tick

import cc.mewcraft.wakame.WakamePlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal class BukkitTicker(
    private val plugin: WakamePlugin
) : Ticker {
    private val scheduler: BukkitScheduler
        get() = plugin.server.scheduler

    override fun addTick(tickable: AlwaysTickable): Int {
        return addTick(tickable as Tickable)
    }

    override fun addTick(skillTick: Tickable): Int {
        val runnable = TickRunnable(skillTick)
        val taskId = runnable.runTaskTimer(plugin, 0, 1).taskId
        return taskId
    }

    override fun stopTick(taskId: Int) {
        scheduler.cancelTask(taskId)
    }
}

private class TickRunnable(
    val tick: Tickable
) : BukkitRunnable() {
    override fun run() {
        try {
            val result = tick.tick()
            if (result != TickResult.CONTINUE_TICK) {
                cancel()
                return
            }
        } catch (t: Throwable) {
            TickerSupport.logger.error("Error tick in ${tick.javaClass.simpleName}", t)
            cancel()
        }
        tick.tickCount++
    }

    override fun cancel() {
        tick.whenRemove()
        super.cancel()
    }
}

private object TickerSupport : KoinComponent {
    val logger: Logger by inject()
}