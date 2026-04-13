package cc.mewcraft.wakame.item.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * **原版重锤**武器的配置.
 *
 * @property attackCooldown 攻击后物品冷却.
 * @property attackDamageRatePerFallHeight 每单位下落高度增加的伤害的系数.
 * @property damageByFallHeightLimit 下落高度增加的额外伤害的上限.
 */
@ConfigSerializable
data class Mace(
    override val attackCooldown: Int = 33,
    val attackDamageRatePerFallHeight: Double = 1.0,
    val damageByFallHeightLimit: Double = 100.0,
) : ConfigurableAttackCooldown

/**
 * **原版近战(斧, 镐, 锄等单体武器)**武器的配置.
 *
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class Melee(
    override val attackCooldown: Int = 20,
) : ConfigurableAttackCooldown

/**
 * **原版横扫**武器的配置.
 *
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class Sweep(
    override val attackCooldown: Int = 12,
) : ConfigurableAttackCooldown

/**
 * **原版三叉戟**武器的配置.
 *
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class Trident(
    override val attackCooldown: Int = 18,
) : ConfigurableAttackCooldown