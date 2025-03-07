package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class BukkitEntityBridge : IteratingSystem(
    family = FamilyDefinitions.BUKKIT_ENTITY
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].bukkitEntity
        if (bukkitEntity.isValid) {
            entity.remove()
        }
    }
}