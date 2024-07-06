package cc.mewcraft.wakame.tick

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

/**
 * 用于处理 [Tickable].
 */
internal object WakameTicker {
    private val ticks: MutableMap<UUID, Tickable> = Object2ObjectOpenHashMap()

    internal fun tick() {
        for ((index, child) in ticks) {
            val result = child.tick()
            if (result != TickResult.CONTINUE_TICK) {
                ticks.remove(index)
            }
        }
    }

    fun addTickAlwaysExecuted(runnable: Runnable): UUID {
        val tickable = Tickable {
            runnable.run()
            TickResult.CONTINUE_TICK
        }
        return addTick(tickable)
    }

    fun addTick(skillTick: Tickable): UUID {
        val tickId = UUID.randomUUID()
        ticks[tickId] = skillTick
        return tickId
    }

    fun stopTick(tickId: UUID) {
        ticks.remove(tickId)
    }

    fun stopTick(skillTick: Tickable) {
        ticks.entries.removeIf { it.value == skillTick }
    }
}

internal class TickerListener : Listener {
    @EventHandler
    private fun onServerTickStart(event: ServerTickStartEvent) {
        WakameTicker.tick()
    }
}