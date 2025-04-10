package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EEntity
import com.github.quillraven.fleks.Component


class EntityBlock(entity: EEntity) : EntityRef(entity), Component<EntityBlock> {

    companion object : EComponentType<EntityBlock>()

    override fun type() = EntityBlock

}