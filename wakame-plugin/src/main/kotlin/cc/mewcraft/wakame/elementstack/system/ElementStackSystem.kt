package cc.mewcraft.wakame.elementstack.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.elementstack.component.ElementStackComponent
import cc.mewcraft.wakame.elementstack.component.ElementStackContainer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class ElementStackSystem : IteratingSystem(
    family = Families.ELEMENT_STACK
) {
    override fun onTickEntity(entity: Entity) {
        val target = entity[TargetTo].target
        if (target !in world) {
            entity.remove()
            return
        }

        val tickCountComponent = entity[TickCountComponent]
        val elementStackComponent = entity[ElementStackComponent]

        // 如果 stackCount 小于等于 0，移除实体
        if (elementStackComponent.amount <= 0) {
            removeEntity(entity, target)
            return
        }

        // 如果 tickCount 达到设置好的时间, 移除异常效果.
        if (tickCountComponent.tick >= elementStackComponent.disappearTick) {
            removeEntity(entity, target)
        }
    }

    private fun removeEntity(entity: Entity, target: Entity) {
        LOGGER.info("在 ${KoishEntity(entity)} 上的元素效果已失效.")
        target[ElementStackContainer].remove(entity[ElementComponent].element)
        entity.remove()
    }
}