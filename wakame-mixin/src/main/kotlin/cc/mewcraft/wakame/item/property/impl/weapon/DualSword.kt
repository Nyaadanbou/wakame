package cc.mewcraft.wakame.item.property.impl.weapon

import org.joml.Vector3f
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 剑的配置.
 *
 * @property itemDamagePerAttack 每次攻击命中消耗的耐久度.
 * @property attackHalfExtentsBase OBB攻击判定范围.
 * @property attackCooldown 攻击后物品冷却.
 */
@ConfigSerializable
data class DualSword(
    val itemDamagePerAttack: Int = 1,
    val attackHalfExtentsBase: Vector3f = Vector3f(1.3f, 0.05f, 1.4f),
    val attackCooldown: Int = 12,
)
