package cc.mewcraft.wakame.enchantment2.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class AutoMelting(
    val activated: Boolean
) : Component<AutoMelting> {

    companion object : ComponentType<AutoMelting>()

    override fun type() = AutoMelting

}