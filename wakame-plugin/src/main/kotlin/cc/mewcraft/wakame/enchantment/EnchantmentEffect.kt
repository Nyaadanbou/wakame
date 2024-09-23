package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.user.User

/**
 * 表示一个附魔效果.
 */
sealed interface EnchantmentEffect {
    companion object {
        fun attribute(effect: Map<Attribute, AttributeModifier>): EnchantmentEffect {
            return AttributeEnchantmentEffect(effect)
        }
    }

    fun apply(user: User<*>)

    fun remove(user: User<*>)
}

private class AttributeEnchantmentEffect(
    private val effect: Map<Attribute, AttributeModifier>,
) : EnchantmentEffect {
    override fun apply(user: User<*>) {
        val attributeMap = user.attributeMap
        for ((attribute, modifier) in effect) {
            if (attributeMap.hasModifier(attribute, modifier.id))
                return
            attributeMap.getInstance(attribute)?.addModifier(modifier)
        }
    }

    override fun remove(user: User<*>) {
        effect.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.removeModifier(modifier) }
    }
}

