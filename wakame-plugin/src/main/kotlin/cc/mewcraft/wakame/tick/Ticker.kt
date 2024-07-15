package cc.mewcraft.wakame.tick

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.*

/**
 * 用于处理 [Tickable].
 */
internal object Ticker : KoinComponent {
    private val logger: Logger by inject()

    private val ticksToAdd: MutableMap<UUID, Tickable> = Object2ObjectOpenHashMap()
    private val tickToRemove: MutableList<UUID> = ObjectArrayList()

    private val ticks: MutableMap<UUID, Tickable> = Object2ReferenceArrayMap()
    private val tickableToTicks: MutableMap<Tickable, Long> = Reference2LongArrayMap()

    internal fun tickStart() {
        for (tickId in tickToRemove) {
            val tickable = ticks.remove(tickId)
            try {
                tickable?.whenRemove()
            } catch (t: Throwable) {
                logger.error("Error occurred while removing $tickable", t)
            } finally {
                tickable?.let { tickableToTicks.remove(it) }
            }
        }
        tickToRemove.clear()
        ticks.putAll(ticksToAdd)
        for ((_, value) in ticksToAdd) {
            tickableToTicks[value] = 0
        }
        ticksToAdd.clear()
    }

    internal fun tickEnd() {
        for ((index, child) in ticks) {
            try {
                val tickCount = tickableToTicks[child]!!
                val result = child.tick(tickCount)
                if (result != TickResult.CONTINUE_TICK) {
                    tickToRemove.add(index)
                }
                tickableToTicks.merge(child, 1, Long::plus)
            } catch (t: Throwable) {
                logger.error("Error occurred while ticking $child", t)
                tickToRemove.add(index)
            }
        }
    }

    fun addTick(tickable: AlwaysTickable): UUID {
        return addTick(tickable as Tickable)
    }

    fun addTick(skillTick: Tickable): UUID {
        val tickId = UUID.randomUUID()
        ticksToAdd[tickId] = skillTick
        return tickId
    }

    fun stopTick(tickId: UUID) {
        tickToRemove.add(tickId)
    }

    fun stopTick(skillTick: Tickable) {
        tickToRemove.add(ticks.entries.find { it.value == skillTick }?.key ?: return)
    }
}

internal class TickerListener : Listener {
    @EventHandler
    private fun onServerTickStart(event: ServerTickStartEvent) {
        Ticker.tickStart()
    }

    @EventHandler
    private fun onServerTickEnd(event: ServerTickEndEvent) {
        Ticker.tickEnd()
    }
}