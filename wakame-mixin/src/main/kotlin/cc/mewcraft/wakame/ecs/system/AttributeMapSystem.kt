package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.entity.attribute.AttributeMapFactory
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AttributeMapSystem : IteratingSystem(
    family = family { all(BukkitObject).any(BukkitPlayerComponent, BukkitEntityComponent) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        // 获取 bukkit entity
        val bukkitEntity = entity.getOrNull(BukkitPlayerComponent)?.bukkitPlayer
            ?: entity[BukkitEntityComponent].bukkitEntity

        // 创建 attribute container
        val attributeContainer = AttributeMapFactory.INSTANCE.create(bukkitEntity)
            ?: return

        // 添加到 ecs entity
        entity.configure { it += attributeContainer }
    }
}