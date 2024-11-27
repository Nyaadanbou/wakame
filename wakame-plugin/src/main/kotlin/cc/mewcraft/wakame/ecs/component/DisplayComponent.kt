package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import net.kyori.adventure.text.Component as TextComponent

data class DisplayComponent(
    var name: TextComponent,
) : Component<IdentifierComponent> {
    override fun type(): ComponentType<IdentifierComponent> = IdentifierComponent

    companion object : ComponentType<IdentifierComponent>()
}
