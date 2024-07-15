package cc.mewcraft.wakame.tick

import cc.mewcraft.wakame.WakamePlugin
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal class BukkitTicker(
    private val plugin: WakamePlugin
) : Ticker {
    private val ticks: MutableSet<TickRunnable> = ReferenceArraySet()

    override fun addTick(tickable: AlwaysTickable): Int {
        return addTick(tickable as Tickable)
    }

    override fun addTick(skillTick: Tickable): Int {
        val runnable = TickRunnable(skillTick)
        val taskId = runnable.runTaskTimer(plugin, 0, 1).taskId
        ticks.add(runnable)
        return taskId
    }

    override fun stopTick(taskId: Int) {
        ticks.find { it.taskId == taskId }?.cancel()
    }

    override fun stopTick(skillTick: Tickable) {
        ticks.find { it.tick == skillTick }?.cancel()
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