package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Fleks
import com.github.quillraven.fleks.Entity

abstract class EntityRef(private val entity: Entity) {
    private val entityOrNull: Entity? get() = entity.takeIf<Entity>(Fleks.world::contains)
    operator fun invoke(): Entity? = entityOrNull
}