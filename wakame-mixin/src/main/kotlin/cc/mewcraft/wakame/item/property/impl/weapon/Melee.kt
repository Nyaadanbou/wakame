package cc.mewcraft.wakame.item.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 一般近战武器的配置.
 *
 * @property itemDamagePerAttack 每次攻击命中消耗的耐久度.
 * @property attackRange 攻击距离.
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class Melee(
    val itemDamagePerAttack: Int = 1,
    val attackRange: Double = 3.0,
    val attackCooldown: Int = 20,
)
