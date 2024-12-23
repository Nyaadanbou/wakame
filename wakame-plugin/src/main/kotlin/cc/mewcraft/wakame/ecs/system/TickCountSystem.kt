package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class TickCountSystem : IteratingSystem(
    family = family { all(TickCountComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[TickCountComponent].tick += deltaTime
    }
}