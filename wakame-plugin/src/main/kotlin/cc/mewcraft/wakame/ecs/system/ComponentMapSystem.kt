package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.ComponentMapComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class ComponentMapSystem : IteratingSystem(
    family = family { all(IdentifierComponent, ComponentMapComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val componentMapComponent = entity[ComponentMapComponent]
        componentMapComponent.componentMap = ComponentMap(world.snapshotOf(entity))
    }
}