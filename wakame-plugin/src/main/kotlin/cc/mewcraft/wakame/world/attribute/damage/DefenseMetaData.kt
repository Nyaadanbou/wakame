package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier

/**
 * 防御元数据
 * 包含了一次伤害中“防御阶段”的有关信息
 */
sealed interface DefenseMetaData {

    /**
     * 用于修改原版的伤害修饰器，可空集
     * 具体见 damage_system.md
     */
    val damageModifiers: Map<DamageModifier, Double>
    fun calculateFinalDamage(damageMetaData: DamageMetaData): Double
}

/**
 * 原版防御的元数据
 * 不做任何除了 [damageModifiers] 之外的处理
 */
class VanillaDefenseMetaData(
    override val damageModifiers: Map<DamageModifier, Double> = emptyMap()
) : DefenseMetaData {

    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        return damageMetaData.damageValue
    }
}

/**
 * 玩家防御的元数据
 * 玩家具有元素防御力
 */
class PlayerDefenseMetaData(
    override val damageModifiers: Map<DamageModifier, Double>,
    val user: User<Player>,
) : DefenseMetaData {

    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        when (damageMetaData) {
            /**
             * 玩家受到原版伤害时
             * 使用默认元素进行减伤
             */
            is VanillaDamageMetaData -> {
                return DefenseUtils.getDamageAfterDefense(
                    damageMetaData.damageValue,
                    user.attributeMap.getValue(Attributes.byElement(ElementRegistry.DEFAULT).DEFENSE),
                    0.0
                )
            }

            /**
             * 玩家受到其他玩家的伤害时
             * 使用全部元素进行减伤
             */
            is PlayerMeleeAttackMetaData -> {
                return damageMetaData.packets.sumOf {
                    DefenseUtils.getDamageAfterDefense(
                        it.finalDamage,
                        user.attributeMap.getValue(Attributes.byElement(it.element).DEFENSE),
                        it.defensePenetration
                    )
                }
            }

            is PlayerProjectileMetaData -> TODO()

            is EntityMeleeAttackMetaData -> TODO()
            is EntityProjectileMetaData -> TODO()
        }
    }
}

class EntityDefenseMetaData(
    override val damageModifiers: Map<DamageModifier, Double>,
    entity: LivingEntity
) : DefenseMetaData {
    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        TODO("Not yet implemented")
    }

}

data class ElementDefensePacket(
    val element: Element,
    val defense: Double,
)

/**
 * 防御的工具类
 * 可以添加从配置文件载入的防御计算公式
 */
object DefenseUtils {
    fun getDamageAfterDefense(originalDamage: Double, defense: Double, defensePenetration: Double): Double {
        val validDefense = (defense - defensePenetration).coerceAtLeast(0.0)
        return originalDamage * (1 - (validDefense / 1 * originalDamage + validDefense))
    }
}