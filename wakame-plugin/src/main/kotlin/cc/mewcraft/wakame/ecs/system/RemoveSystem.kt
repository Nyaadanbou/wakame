package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class RemoveSystem : IteratingSystem(
    family = family { all(Remove) }
) {
    override fun onTickEntity(entity: Entity) {
        entity.remove().also { LOGGER.info("Entity ${KoishEntity(entity)} removed") }
    }
}