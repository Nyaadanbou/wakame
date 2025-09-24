package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ecs.CommonFamilies
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

object InitAbilityContainer : IteratingSystem(
    family = CommonFamilies.BUKKIT_ENTITY
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += AbilityContainer() }
    }
}