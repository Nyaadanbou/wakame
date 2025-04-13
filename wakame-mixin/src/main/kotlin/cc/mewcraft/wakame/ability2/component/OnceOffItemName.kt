package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import net.kyori.adventure.text.Component

data class OnceOffItemName(
    val index: Int,
    var component: Component,
    var durationTick: Long,
) : EComponent<OnceOffItemName> {
    var lastComponent: Component = Component.empty()

    companion object : EComponentType<OnceOffItemName>()

    override fun type(): EComponentType<OnceOffItemName> = OnceOffItemName
}
