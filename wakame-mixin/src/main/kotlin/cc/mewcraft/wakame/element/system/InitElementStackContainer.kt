package cc.mewcraft.wakame.element.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.element.component.ElementStackContainer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

object InitElementStackContainer : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += ElementStackContainer() }
    }
}