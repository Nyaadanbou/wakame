package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.element.Element
import org.koin.core.component.get

fun test() {
    val element: Element = Injector.get()

    val defenseGetter: AttributeGetter = Attributes.DEFENSE
    val defenseAttribute: ElementAttribute = defenseGetter.by(element)

    val attackDamageRateGetter: AttributeGetter = Attributes.ATTACK_DAMAGE_RATE
    val attackDamageRateAttribute: ElementAttribute = attackDamageRateGetter.by(element)
}