package cc.mewcraft.wakame.tick

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 用于处理 [Tickable].
 */
interface Ticker {
    companion object : KoinComponent {
        val INSTANCE: Ticker by inject()
    }

    fun addTick(tickable: AlwaysTickable): Int

    fun addTick(skillTick: Tickable): Int

    fun stopTick(taskId: Int)

    fun stopTick(tickable: Tickable)

    operator fun contains(taskId: Int?): Boolean
}