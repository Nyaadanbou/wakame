package cc.mewcraft.wakame.tick

/**
 * 用于处理 [Tickable].
 */
interface Ticker {
    fun addTick(tickable: AlwaysTickable): Int

    fun addTick(skillTick: Tickable): Int

    fun stopTick(taskId: Int)
}