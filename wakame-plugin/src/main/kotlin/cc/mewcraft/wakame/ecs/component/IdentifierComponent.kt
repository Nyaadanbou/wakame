package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class IdentifierComponent(
    val id: Identifier,
) : Component<IdentifierComponent> {
    companion object : ComponentType<IdentifierComponent>()

    override fun type(): ComponentType<IdentifierComponent> = IdentifierComponent
}
