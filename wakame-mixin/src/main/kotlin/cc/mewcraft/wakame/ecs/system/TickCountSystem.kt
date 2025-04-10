package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem


object TickCountSystem : IteratingSystem(
    family = EWorld.family { all(TickCountComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[TickCountComponent].tick++
    }
}