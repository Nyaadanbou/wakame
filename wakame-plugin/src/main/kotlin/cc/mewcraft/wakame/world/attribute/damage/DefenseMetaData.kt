package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * 防御元数据
 * 包含了一次伤害中“防御阶段”的有关信息
 */
sealed interface DefenseMetaData {
    fun calculateFinalDamage(damageMetaData: DamageMetaData): Double
}

/**
 * 无操作的防御元数据
 */
data object NoopDefenseMetaData : DefenseMetaData {

    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        return damageMetaData.damageValue
    }
}

/**
 * 玩家防御元数据
 * 需要计算玩家的元素防御
 */
class PlayerDefenseMetaData(
    val user: User<Player>,
) : DefenseMetaData {

    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        when (damageMetaData) {

            is DefaultDamageMetaData -> {
                return DamageManager.getDamageAfterDefense(
                    damageMetaData.damageValue,
                    user.attributeMap.getValue(Attributes.byElement(ElementRegistry.DEFAULT).DEFENSE),
                    0.0
                )
            }

            is VanillaDamageMetaData -> {
                return DamageManager.getDamageAfterDefense(
                    damageMetaData.damageValue,
                    user.attributeMap.getValue(Attributes.byElement(damageMetaData.element).DEFENSE),
                    damageMetaData.defensePenetration
                )
            }

            /**
             * 玩家受到其他玩家的伤害时
             * 使用全部元素进行减伤
             */
            is PlayerMeleeAttackMetaData, is PlayerProjectileDamageMetaData -> {
                return damageMetaData.packets.sumOf {
                    DamageManager.getDamageAfterDefense(
                        it.finalDamage,
                        user.attributeMap.getValue(Attributes.byElement(it.element).DEFENSE),
                        it.defensePenetration
                    )
                }
            }


            is EntityMeleeAttackMetaData -> TODO()
            is EntityProjectileDamageMetaData -> TODO()
        }
    }
}

/**
 * 非玩家实体防御元数据
 */
class EntityDefenseMetaData(
    entity: LivingEntity
) : DefenseMetaData {
    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        TODO("Not yet implemented")
    }

}