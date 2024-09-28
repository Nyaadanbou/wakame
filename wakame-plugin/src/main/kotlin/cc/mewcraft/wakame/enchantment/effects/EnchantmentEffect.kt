package cc.mewcraft.wakame.enchantment.effects

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player

/**
 * 封装了一个魔咒对玩家提供的效果, 例如属性加成, 额外技能等.
 */
internal sealed interface EnchantmentEffect {
    companion object {
        fun attribute(modifiers: Map<Attribute, AttributeModifier>): EnchantmentEffect {
            return AttributeEnchantmentEffect(modifiers)
        }

        fun skill(todo: Nothing): EnchantmentEffect {
            TODO()
        }
    }

    /**
     * 将该效果应用到玩家 [user] 上.
     */
    fun applyTo(user: User<Player>)

    /**
     * 将该效果从玩家 [user] 身上移除.
     */
    fun removeFrom(user: User<Player>)
}
