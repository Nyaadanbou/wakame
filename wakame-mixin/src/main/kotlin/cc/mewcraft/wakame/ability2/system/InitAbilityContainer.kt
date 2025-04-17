package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.AbilityContainer
import cc.mewcraft.wakame.ecs.Families
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

object InitAbilityContainer : IteratingSystem(
    family = Families.BUKKIT_ENTITY
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += AbilityContainer() }
    }
}