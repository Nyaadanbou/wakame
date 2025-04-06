package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class EntityPlayer(entity: Entity) : EntityRef(entity), Component<EntityPlayer> {

    companion object : ComponentType<EntityPlayer>()

    override fun type() = EntityPlayer

}