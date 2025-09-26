package cc.mewcraft.wakame.enchantment.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

class Fragile(
    val multiplier: Float,
) : Component<Fragile> {

    companion object : EComponentType<Fragile>()

    override fun type() = Fragile

}