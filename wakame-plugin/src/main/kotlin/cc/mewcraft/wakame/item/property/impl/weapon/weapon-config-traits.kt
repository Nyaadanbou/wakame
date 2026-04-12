package cc.mewcraft.wakame.item.property.impl.weapon

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import org.bukkit.entity.Player

/**
 * 此武器类型具有 **攻击冷却** 配置项
 */
interface ConfigurableAttackCooldown {
    val attackCooldown: Int
}

/**
 * 此武器类型具有 **是否双手持握** 配置项
 */
interface ConfigurableTwoHanded {
    val isTwoHanded: Boolean
}

/**
 * 检查玩家是否满足双手持握要求.
 * 若武器不要求双手持握, 返回 true.
 * 若武器要求双手持握且玩家副手无物品, 返回 true.
 * 否则返回 false.
 */
fun ConfigurableTwoHanded.checkTwoHandedRequirement(player: Player): Boolean {
    if (!isTwoHanded) return true
    if (player.inventory.itemInOffHand.isEmpty) return true

    return false
}

/**
 * 玩家不满足双手持握要求时执行的代码.
 */
fun ConfigurableTwoHanded.handleTwoHandedFailure(player: Player) {
    player.sendActionBar(TranslatableMessages.MSG_ERR_REQUIRE_TWO_HANDED.build())
}