package cc.mewcraft.wakame.enchantment.effects.implementation

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player

/**
 * 给玩家提供属性修饰器 ([AttributeModifier]) 加成的 [EnchantmentEffect].
 */
internal class AttributeEnchantmentEffect(
    private val attribute: Attribute,
    private val attributeModifier: AttributeModifier,
) : EnchantmentEffect {
    override fun applyTo(user: User<Player>) {
        user.attributeMap.getInstance(attribute)?.addModifier(attributeModifier)
    }

    override fun removeFrom(user: User<Player>) {
        user.attributeMap.getInstance(attribute)?.removeModifier(attributeModifier)
    }
}