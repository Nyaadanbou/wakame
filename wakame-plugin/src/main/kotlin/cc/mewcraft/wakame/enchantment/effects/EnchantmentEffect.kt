package cc.mewcraft.wakame.enchantment.effects

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player

/**
 * 封装了一个魔咒对玩家提供的效果, 例如属性加成, 额外技能等.
 */
internal interface EnchantmentEffect {
    /**
     * 将该效果应用到玩家 [user] 上.
     */
    fun applyTo(user: User<Player>)

    /**
     * 将该效果从玩家 [user] 身上移除.
     */
    fun removeFrom(user: User<Player>)
}
