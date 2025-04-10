package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EEntity
import com.github.quillraven.fleks.Component

class EntityEntity(entity: EEntity) : EntityRef(entity), Component<EntityEntity> {

    companion object : EComponentType<EntityEntity>()

    override fun type() = EntityEntity

}