package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class EntityEntity(entity: FleksEntity) : EntityRef(entity), Component<EntityEntity> {

    companion object : ComponentType<EntityEntity>()

    override fun type() = EntityEntity

}