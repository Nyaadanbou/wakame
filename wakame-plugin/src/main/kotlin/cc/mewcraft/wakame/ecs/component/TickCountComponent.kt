package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * [tick] 会在每个服务端的 tick 加 1.
 *
 * @see cc.mewcraft.wakame.ecs.system.TickCountSystem
 */
data class TickCountComponent(
    var tick: Int,
) : Component<TickCountComponent> {
    companion object : ComponentType<TickCountComponent>()

    override fun type(): ComponentType<TickCountComponent> = TickCountComponent
}