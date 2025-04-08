package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class EntityEntity(entity: EEntity) : EntityRef(entity), Component<EntityEntity> {

    companion object : ComponentType<EntityEntity>()

    override fun type() = EntityEntity

}