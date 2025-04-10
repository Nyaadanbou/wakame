package cc.mewcraft.wakame.entity.attribute.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.attribute.AttributeMapFactory
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

/**
 * 负责给玩家/生物初始化属性的容器.
 */
object InitAttributeContainer : IteratingSystem(
    family = EWorld.family { all(BukkitObject).any(BukkitPlayer, BukkitEntity) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        // 获取 bukkit entity
        val bukkitEntity = entity.getOrNull(BukkitPlayer)?.unwrap()
            ?: entity[BukkitEntity].unwrap()

        // 创建 attribute container
        val attributeContainer = AttributeMapFactory.INSTANCE.create(bukkitEntity)
            ?: return

        // 添加到 ecs entity
        entity.configure { it += attributeContainer }
    }
}