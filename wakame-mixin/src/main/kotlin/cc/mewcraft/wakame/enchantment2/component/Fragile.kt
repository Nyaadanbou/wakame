package cc.mewcraft.wakame.enchantment2.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class Fragile(
    val multiplier: Float,
) : Component<Fragile> {

    companion object : ComponentType<Fragile>()

    override fun type() = Fragile

}