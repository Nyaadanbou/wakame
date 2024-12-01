package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.external.ComponentMap

/**
 * 代表了一个执行结果, 但是具体的执行逻辑需要通过 ECS 的 system 执行.
 */
interface Result {
    fun tick(tickCount: Double, componentMap: ComponentMap)
}