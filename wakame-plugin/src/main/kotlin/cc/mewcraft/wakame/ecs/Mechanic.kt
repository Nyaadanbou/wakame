package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap

/**
 * 代表了一个执行机制.
 *
 * 由 ECS 的 system 执行.
 */
interface Mechanic {

    /**
     * 当此 [Mechanic] 添加到 ECS 中时, 会调用这个方法.
     */
    fun onEnable(componentMap: ComponentMap) = Unit

    /**
     * 当此 [Mechanic] 进行 tick 时, 会调用这个方法.
     */
    fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult

    /**
     * 当此 [Mechanic] 被移除时, 会调用这个方法.
     */
    fun onDisable(componentMap: ComponentMap) = Unit
}

fun Mechanic(
    onEnable: (ComponentMap) -> Unit = {},
    tick: (deltaTime: Double, tickCount: Double, ComponentMap) -> TickResult,
    onDisable: (ComponentMap) -> Unit = {},
): Mechanic {
    return object : Mechanic {
        override fun onEnable(componentMap: ComponentMap) = onEnable(componentMap)
        override fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = tick(deltaTime, tickCount, componentMap)
        override fun onDisable(componentMap: ComponentMap) = onDisable(componentMap)
    }
}