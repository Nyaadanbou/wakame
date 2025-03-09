package cc.mewcraft.wakame.element.effect

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.StackCountComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import me.lucko.helper.text3.mini

class IgniteSystem : IteratingSystem(
    family = family { all(EntityType.ELEMENT_STACK, ElementComponent, StackCountComponent, TargetTo) }
) {
    override fun onTickEntity(entity: Entity) {
        val element = entity[ElementComponent].element
        val count = entity[StackCountComponent].count
        val target = entity[TargetTo].target
        val bukkitEntity = target.bukkitEntity ?: return
        if (!ElementEffectRegistryConfigStorage[element, count].contains(ElementEffects.IGNITE)) {
            // 关闭效果
            bukkitEntity.isVisualFire = false
            return
        }

        // 开启效果
        bukkitEntity.isVisualFire = true
        SERVER.broadcast("${bukkitEntity.name}: 好烫!".mini)
    }
}