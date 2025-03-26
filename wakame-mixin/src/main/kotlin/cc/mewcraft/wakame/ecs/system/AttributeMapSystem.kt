package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.AttributeMapComponent
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

class AttributeMapSystem : IteratingSystem(
    family = Families.BUKKIT_PLAYER
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += AttributeMapComponent(entity[BukkitPlayerComponent].bukkitPlayer) }
    }
}