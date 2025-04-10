package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

/**
 * [tick] 会在每个服务端的 tick 加 1.
 *
 * @see cc.mewcraft.wakame.ecs.system.CountTick
 */
data class TickCount(
    var tick: Int,
) : Component<TickCount> {
    companion object : EComponentType<TickCount>()

    override fun type(): EComponentType<TickCount> = TickCount
}