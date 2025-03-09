package cc.mewcraft.wakame.elementstack.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementStackComponent(
    var amount: Int = 1,
    var maxAmount: Int = 10,
    var disappearTick: Int = 500,
) : Component<ElementStackComponent> {
    companion object : ComponentType<ElementStackComponent>()

    override fun type(): ComponentType<ElementStackComponent> = ElementStackComponent
}