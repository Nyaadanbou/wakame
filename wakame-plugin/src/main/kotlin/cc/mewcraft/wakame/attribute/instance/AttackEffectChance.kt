package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 元素攻击效果触发概率 %
 */
class AttackEffectChance : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "attack_effect_chance")
}