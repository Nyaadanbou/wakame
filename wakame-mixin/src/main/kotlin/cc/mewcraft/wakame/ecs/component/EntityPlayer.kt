package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

import com.github.quillraven.fleks.Entity

class EntityPlayer(entity: Entity) : EntityRef(entity), Component<EntityPlayer> {

    companion object : EComponentType<EntityPlayer>()

    override fun type() = EntityPlayer

}