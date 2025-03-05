package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class MechanicSystem : IteratingSystem(
    family = FamilyDefinitions.MECHANIC
) {
    override fun onTickEntity(entity: Entity) {
        val tick = entity[TickCountComponent].tick
        val result = entity[MechanicComponent].mechanic
        val koishEntity = KoishEntity(entity)

        val tickResult = result.tick(deltaTime.toDouble(), tick, koishEntity)

        entity.configure {
            it += TickResultComponent(tickResult)
        }
    }
}