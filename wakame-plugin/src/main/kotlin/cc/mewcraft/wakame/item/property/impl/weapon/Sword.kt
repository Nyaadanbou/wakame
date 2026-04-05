package cc.mewcraft.wakame.item.property.impl.weapon

import org.joml.Vector3f
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 剑的配置.
 *
 * @property itemDamagePerAttack 每次攻击命中消耗的耐久度.
 * @property attackHalfExtentsBase OBB攻击判定范围.
 * @property attackCooldown 攻击后物品冷却.
 * @property twoHanded 是否需要双手持握才能攻击.
 * @property damageDistributed 是否将伤害均分到所有命中的生物.
 * @property minDamageDistributedRatio 每个命中的生物最少分到的伤害比例, [damageDistributed] 为 true 时此项才有效.
 */
@ConfigSerializable
data class Sword(
    val itemDamagePerAttack: Int = 1,
    val attackHalfExtentsBase: Vector3f = Vector3f(1.3f, 0.05f, 1.4f),
    val attackCooldown: Int = 12,
    val twoHanded: Boolean = false,
    val damageDistributed: Boolean = false,
    val minDamageDistributedRatio: Double = .0,
)
