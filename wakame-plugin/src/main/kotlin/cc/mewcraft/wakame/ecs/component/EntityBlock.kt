package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Fleks
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class EntityBlock(
    private val entity: Entity,
) : Component<EntityBlock> {

    val entityOrNull: Entity? get() = entity.takeIf<Entity>(Fleks.world::contains)

    companion object : ComponentType<EntityBlock>()

    override fun type() = EntityBlock

}