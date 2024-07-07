package cc.mewcraft.wakame.tick

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.HashMap
import java.util.UUID

/**
 * 用于处理 [Tickable].
 */
internal object Ticker {
    private val ticksToAdd: MutableList<Pair<UUID, Tickable>> = ArrayList()
    private val tickToRemove: MutableList<UUID> = ArrayList()

    private val ticks: MutableMap<UUID, Tickable> = HashMap()

    internal fun tickStart() {
        tickToRemove.forEach { ticks.remove(it) }
        tickToRemove.clear()
        ticks.putAll(ticksToAdd)
        ticksToAdd.clear()
    }

    internal fun tickEnd() {
        for ((index, child) in ticks) {
            val result = child.tick()
            if (result != TickResult.CONTINUE_TICK) {
                tickToRemove.add(index)
            }
        }
    }

    fun addTick(tickable: AlwaysTickable): UUID {
        return addTick(tickable as Tickable)
    }

    fun addTick(skillTick: Tickable): UUID {
        val tickId = UUID.randomUUID()
        ticksToAdd.add(tickId to skillTick)
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