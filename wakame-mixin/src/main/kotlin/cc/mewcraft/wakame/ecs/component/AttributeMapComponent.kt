package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AttributeMapComponent(
    private val bukkitEntity: BukkitEntity,
) : Component<AttributeMapComponent> {
    companion object : ComponentType<AttributeMapComponent>()

    override fun type(): ComponentType<AttributeMapComponent> = AttributeMapComponent

    operator fun invoke(): AttributeMap {
        return AttributeMapAccess.INSTANCE.get(bukkitEntity).getOrThrow()
    }
}
