package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ability.component.AbilityComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class AbilityInitSystem : IteratingSystem(
    family = Families.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        entity[AbilityComponent].isReadyToRemove = false
    }
}