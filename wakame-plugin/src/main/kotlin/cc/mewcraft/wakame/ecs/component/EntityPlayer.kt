package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class EntityPlayer(entity: FleksEntity) : EntityRef(entity), Component<EntityPlayer> {

    companion object : ComponentType<EntityPlayer>()

    override fun type() = EntityPlayer

}