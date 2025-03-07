package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class AbilityInitSystem : IteratingSystem(
    family = FamilyDefinitions.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        entity[AbilityComponent].isReadyToRemove = false
    }
}