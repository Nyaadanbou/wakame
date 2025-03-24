package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Fleks
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class EntityPlayer(
    private val entity: Entity,
) : Component<EntityPlayer> {

    val entityOrNull: Entity? get() = entity.takeIf<Entity>(Fleks.world::contains)

    companion object : ComponentType<EntityPlayer>()

    override fun type() = EntityPlayer

}