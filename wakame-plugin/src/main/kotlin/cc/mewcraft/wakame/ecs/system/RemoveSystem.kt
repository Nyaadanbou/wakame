package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.Remove
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class RemoveSystem : IteratingSystem(
    family = family { all(Remove) }
) {
    override fun onTickEntity(entity: Entity) {
        entity.remove()
    }
}