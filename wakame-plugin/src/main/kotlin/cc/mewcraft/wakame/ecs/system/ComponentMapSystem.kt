package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.ComponentMapComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import com.github.quillraven.fleks.IntervalSystem

class ComponentMapSystem : IntervalSystem() {
    override fun onTick() {
        world.forEach { entity ->
            if (entity.hasNo(ComponentMapComponent)) {
                val componentMapComponent = ComponentMapComponent(ComponentMap(world.snapshotOf(entity)))
                entity.configure {
                    it += componentMapComponent
                }
            } else {
                val componentMapComponent = entity[ComponentMapComponent]
                world.loadSnapshotOf(entity, componentMapComponent.componentMap.toSnapshot())
            }
        }
    }
}