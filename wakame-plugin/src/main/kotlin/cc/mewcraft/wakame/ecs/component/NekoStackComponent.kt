package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.item.NekoStack
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class NekoStackComponent(
    var nekoStack: NekoStack
): Component<NekoStackComponent> {
    override fun type(): ComponentType<NekoStackComponent> = NekoStackComponent

    companion object : ComponentType<NekoStackComponent>()
}