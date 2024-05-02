package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier

/**
 * 防御元数据
 * 包含了一次伤害中“防御阶段”的有关信息
 * 其中 [damageModifierPackets] 用于修改原版的伤害修饰器，可空集
 * 具体见 damage_system.md
 */
sealed interface DefenseMetaData {
    val damageMetaData: DamageMetaData
    val damageModifierPackets: List<DamageModifierPacket>
    val finalDamage: Double
}

/**
 * 原版防御的元数据
 * 不做任何除了 [damageModifierPackets] 之外的处理
 */
class VanillaDefenseMetaData(
    override val damageMetaData: DamageMetaData,
    override val damageModifierPackets: List<DamageModifierPacket>
) : DefenseMetaData {
    constructor(
        damageMetaData: DamageMetaData,
    ) : this(damageMetaData, emptyList())

    override val finalDamage: Double = calculateFinalDamage()

    private fun calculateFinalDamage(): Double {
        return damageMetaData.damageValue
    }
}

/**
 * 玩家防御的元数据
 * 玩家具有元素防御力
 */
class PlayerDefenseMetaData(
    override val damageMetaData: DamageMetaData,
    override val damageModifierPackets: List<DamageModifierPacket>,
    val user: User<Player>
) : DefenseMetaData {
    override val finalDamage: Double = calculateFinalDamage()

    private fun calculateFinalDamage(): Double {
        when (damageMetaData) {
            /**
             * 玩家受到原版伤害时
             * 使用默认元素进行减伤
             */
            is VanillaDamageMetaData -> {
                return DefenseUtils.getDamageAfterDefense(
                    damageMetaData.damageValue,
                    user.attributeMap.getValue(Attributes.byElement(ElementRegistry.DEFAULT).DEFENSE)
                )
            }

            is PlayerMeleeAttackMetaData -> {
                var damage = 0.0
                damageMetaData.packets.forEach {
                    damage += DefenseUtils.getDamageAfterDefense(
                        it.finalDamage,
                        user.attributeMap.getValue(Attributes.byElement(it.element).DEFENSE)
                    )
                }
                return damage
            }

            is PlayerProjectileMetaData -> TODO()

            is EntityMeleeAttackMetaData -> TODO()
            is EntityProjectileMetaData -> TODO()
        }
    }
}

data class DamageModifierPacket(
    val damageModifier: DamageModifier,
    val rate: Double
)

/**
 * 防御的工具类
 * 可以添加从配置文件载入的防御计算公式
 */
object DefenseUtils {
    fun getDamageAfterDefense(originalDamage: Double, defense: Double): Double {
        return originalDamage * (1 - (defense / 1 * originalDamage + defense))
    }
}