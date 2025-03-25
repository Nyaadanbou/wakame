package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ecs.Families
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class AbilityInitSystem : IteratingSystem(
    family = Families.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        entity[AbilityComponent].isReadyToRemove = false
    }
}