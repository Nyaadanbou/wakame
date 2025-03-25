package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class EntityBlock(entity: FleksEntity) : EntityRef(entity), Component<EntityBlock> {

    companion object : ComponentType<EntityBlock>()

    override fun type() = EntityBlock

}