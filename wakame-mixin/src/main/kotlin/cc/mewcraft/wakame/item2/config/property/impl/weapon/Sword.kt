package cc.mewcraft.wakame.item2.config.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 剑的配置.
 *
 * @property attackHalfWidth 攻击判定OBB的宽度.
 * @property attackHalfHeight 攻击判定OBB的高度.
 * @property attackHalfDepth 攻击判定OBB的深度.
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class Sword(
    val attackHalfWidth: Float = 1.4f,
    val attackHalfHeight: Float = 0.05f,
    val attackHalfDepth: Float = 1.5f,
    val attackCooldown: Int = 12,
    override val cancelVanillaDamage: Boolean = false,
) : WeaponBase()
