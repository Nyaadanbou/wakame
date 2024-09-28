package cc.mewcraft.wakame.enchantment.effects

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player

/**
 * 给玩家提供属性的 [EnchantmentEffect].
 */
internal class AttributeEnchantmentEffect(
    private val effect: Map<Attribute, AttributeModifier>,
) : EnchantmentEffect {
    override fun applyTo(user: User<Player>) {
        val attributeMap = user.attributeMap
        for ((type, modifier) in effect) {
            attributeMap.getInstance(type)?.addModifier(modifier)
        }
    }

    override fun removeFrom(user: User<Player>) {
        val attributeMap = user.attributeMap
        for ((type, modifier) in effect) {
            attributeMap.getInstance(type)?.removeModifier(modifier)
        }
    }
}

/**
 * 给玩家提供技能的 [EnchantmentEffect].
 */
internal class SkillEnchantmentEffect(
    private val todo: Nothing,
) : EnchantmentEffect {
    override fun applyTo(user: User<Player>) {
        TODO()
    }

    override fun removeFrom(user: User<Player>) {
        TODO()
    }
}