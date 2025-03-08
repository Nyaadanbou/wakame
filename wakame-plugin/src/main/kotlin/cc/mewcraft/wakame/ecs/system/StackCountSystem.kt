package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.component.StackCountComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class StackCountSystem : IteratingSystem(
    family = family { all(StackCountComponent, TickCountComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickCountComponent = entity[TickCountComponent]
        val stackCountComponent = entity[StackCountComponent]

        // 如果 stackCount 小于等于 0，移除实体
        if (stackCountComponent.count <= 0) {
            entity.remove()
            return
        }

        // 如果 tickCount 达到设置好的时间, 移除异常效果.
        if (tickCountComponent.tick >= stackCountComponent.disappearTick) {
            LOGGER.info("在 ${KoishEntity(entity)} 上的元素效果已失效.")
            entity.remove()
        }
    }
}