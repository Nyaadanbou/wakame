package cc.mewcraft.wakame.world.attribute

import cc.mewcraft.wakame.element.Element

interface DamagePacket {
    val element: Element
    val value: Double
    val isCritical: Boolean
}