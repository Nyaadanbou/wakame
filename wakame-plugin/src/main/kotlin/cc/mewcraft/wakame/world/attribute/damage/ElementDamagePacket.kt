package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.element.Element
import me.lucko.helper.random.VariableAmount
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

data class ElementDamagePacket(
    val element: Element,
    val min: Double,
    val max: Double,
    val rate: Double,
    val defensePenetration: Double,
    val defensePenetrationRate: Double,
) {
    val value: Double = if (min >= max) max else VariableAmount.range(min, max).amount
    val packetDamage: Double = value
}

/**
 * 封装了一个或多个 [ElementDamagePacket].
 */
class DamagePacketBundle:KoinComponent {
    private val logger: Logger by inject()

    val packets = mutableMapOf<Element, ElementDamagePacket>()

    /**
     * 向伤害包中添加元素伤害包
     * 元素重复时，后添加的会替代掉先添加的并警告
     */
    fun addPacket(elementDamagePacket: ElementDamagePacket) {
        val previous = packets.put(elementDamagePacket.element, elementDamagePacket)
        if(previous!=null){
            logger.warn("same type of element has been added to damage bundle!")
        }
    }

    fun addPacketForAllElement()
}