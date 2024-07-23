package cc.mewcraft.wakame.tick

import cc.mewcraft.wakame.WakamePlugin
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger


internal class BukkitTicker(
    private val plugin: WakamePlugin
) : Ticker {
    private val tasks: Int2ObjectOpenHashMap<TickRunnable> = Int2ObjectOpenHashMap()

    override fun schedule(tickable: AlwaysTickable): Int {
        return schedule(tickable as Tickable)
    }

    override fun schedule(skillTick: Tickable): Int {
        val runnable = TickRunnable(skillTick)
        val taskId = runnable.runTaskTimer(plugin, 0, 1).taskId
        tasks[taskId] = runnable
        return taskId
    }

    override fun stopTick(taskId: Int) {
        val runnable = tasks.remove(taskId)
        runnable?.cancel()
    }

    override fun stopTick(tickable: Tickable) {
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.tick == tickable) {
                entry.value.cancel()
                iterator.remove()
            }
        }
    }

    override fun contains(taskId: Int?): Boolean {
        if (taskId == null) return false
        return tasks.containsKey(taskId)
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