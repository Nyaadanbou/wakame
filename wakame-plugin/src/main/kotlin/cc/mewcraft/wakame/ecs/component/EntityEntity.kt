package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Fleks
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class EntityEntity(
    private val entity: Entity,
) : Component<EntityEntity> {

    val entityOrNull: Entity? get() = entity.takeIf<Entity>(Fleks.world::contains)

    companion object : ComponentType<EntityEntity>()

    override fun type() = EntityEntity

}