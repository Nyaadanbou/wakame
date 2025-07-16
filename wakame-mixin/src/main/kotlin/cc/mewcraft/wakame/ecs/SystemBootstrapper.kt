package cc.mewcraft.wakame.ecs

import com.github.quillraven.fleks.IntervalSystem

/**
 * 用于在 ECS 内启动 [com.github.quillraven.fleks.IntervalSystem] 的引导器接口.
 */
fun interface SystemBootstrapper {
    companion object {
        val EMPTY = SystemBootstrapper { null }
    }

    /**
     * 启动一个 [IntervalSystem] 实例.
     *
     * @return 返回一个 [IntervalSystem] 实例或 null.
     */
    fun bootstrap(): IntervalSystem?
}