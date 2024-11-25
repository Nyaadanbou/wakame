package cc.mewcraft.wakame.skill2.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class IdentifierComponent(
    var id: String,
) : Component<IdentifierComponent> {
    override fun type(): ComponentType<IdentifierComponent> = IdentifierComponent

    companion object : ComponentType<IdentifierComponent>()
}
