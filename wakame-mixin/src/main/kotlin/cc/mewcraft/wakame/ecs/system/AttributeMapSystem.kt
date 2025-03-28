package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.AttributeMapComponent
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AttributeMapSystem : IteratingSystem(
    family = family { all(BukkitObject).any(BukkitPlayerComponent, BukkitEntityComponent) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        val attributable = when {
            entity.has(BukkitPlayerComponent) -> entity[BukkitPlayerComponent].bukkitPlayer
            entity.has(BukkitEntityComponent) -> entity[BukkitEntityComponent].bukkitEntity
            else -> return
        }

        entity.configure { it += AttributeMapComponent(attributable) }
    }
}