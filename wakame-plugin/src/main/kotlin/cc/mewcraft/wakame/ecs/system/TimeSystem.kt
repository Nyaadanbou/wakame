package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.TimeComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class TimeSystem : IteratingSystem(
    family = family { all(TimeComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[TimeComponent].time += deltaTime
    }
}