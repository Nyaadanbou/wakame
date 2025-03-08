package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 每 tick 自动移除已经失效的 BukkitEntity Entity.
 */
class BukkitEntityBridge : IteratingSystem(
    family = Families.BUKKIT_ENTITY
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity
        if (bukkitEntity.isValid) {
            entity.remove()
        }
    }
}