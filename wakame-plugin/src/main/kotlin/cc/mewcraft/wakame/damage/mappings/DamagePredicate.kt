@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.util.krequire
import org.bukkit.damage.DamageType
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * 用于检查某次伤害事件相关场景的谓词.
 * 这里相当于直接把 [EntityDamageEvent] 当上下文用.
 */
sealed interface DamagePredicate {
    fun test(event: EntityDamageEvent): Boolean
}

/**
 * 检查攻击实体某个特定数据的谓词.
 * 目前支持检查的数据在伴生类中列举.
 */
data class EntityDataPredicate(
    val requiredData: Map<String, Int>,
) : DamagePredicate {
    companion object {
        const val TYPE_KEY = "entity_data"

        val map: Map<String, (Int, LivingEntity) -> Boolean> = mapOf(
            "adult" to ::testAdult,
            "size" to ::testSize,
            "puff_state" to ::testPuffState,
        )

        private fun testAdult(value: Int, entity: LivingEntity): Boolean {
            if (entity !is Ageable) return false
            return (value > 0) == entity.isAdult
        }

        private fun testSize(value: Int, entity: LivingEntity): Boolean {
            if (entity !is Slime) return false
            return value == entity.size
        }

        private fun testPuffState(value: Int, entity: LivingEntity): Boolean {
            if (entity !is PufferFish) return false
            return value == entity.puffState
        }
    }

    override fun test(event: EntityDamageEvent): Boolean {
        val damager = event.damageSource.causingEntity as? LivingEntity ?: return false
        return requiredData.all { (str, i) -> map[str]?.invoke(i, damager) == true }
    }
}

/**
 * 检查伤害类型的谓词.
 */
data class DamageTypePredicate(
    val types: List<DamageType>,
) : DamagePredicate {
    companion object {
        const val TYPE_KEY = "damage_type"
    }

    override fun test(event: EntityDamageEvent): Boolean {
        return types.contains(event.damageSource.damageType)
    }
}

/**
 * 检查来源实体类型的谓词.
 */
data class CausingEntityTypePredicate(
    val types: List<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_KEY = "causing_entity_type"
    }

    override fun test(event: EntityDamageEvent): Boolean {
        val directEntity = event.damageSource.causingEntity ?: return false
        return types.contains(directEntity.type)
    }
}

/**
 * 检查直接实体类型的谓词.
 */
data class DirectEntityTypePredicate(
    val types: List<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_KEY = "direct_entity_type"
    }

    override fun test(event: EntityDamageEvent): Boolean {
        val directEntity = event.damageSource.directEntity ?: return false
        return types.contains(directEntity.type)
    }
}

/**
 * 检查受伤实体类型的谓词.
 */
data class VictimEntityTypePredicate(
    val types: List<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_KEY = "victim_entity_type"
    }

    override fun test(event: EntityDamageEvent): Boolean {
        return types.contains(event.entity.type)
    }
}

internal object DamagePredicateSerializer : TypeSerializer<DamagePredicate> {

    override fun deserialize(type: Type, node: ConfigurationNode): DamagePredicate {
        val key = node.key().toString()
        return when (key) {
            EntityDataPredicate.TYPE_KEY -> {
                val map = node.childrenMap().mapKeys { (nodeKey, _) ->
                    nodeKey.toString()
                }.mapValues { (_, nodeValue) ->
                    nodeValue.krequire<Int>()
                }
                return EntityDataPredicate(map)
            }

            DamageTypePredicate.TYPE_KEY -> {
                val damageTypes = node.getList<DamageType>(emptyList())
                return DamageTypePredicate(damageTypes)
            }

            CausingEntityTypePredicate.TYPE_KEY -> {
                val entityTypes = node.getList<EntityType>(emptyList())
                return CausingEntityTypePredicate(entityTypes)
            }

            DirectEntityTypePredicate.TYPE_KEY -> {
                val entityTypes = node.getList<EntityType>(emptyList())
                return DirectEntityTypePredicate(entityTypes)
            }

            VictimEntityTypePredicate.TYPE_KEY -> {
                val entityTypes = node.getList<EntityType>(emptyList())
                return VictimEntityTypePredicate(entityTypes)
            }

            else -> {
                throw SerializationException("Unknown damage predicate type: '$key'")
            }
        }
    }

    override fun serialize(type: Type, obj: DamagePredicate?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}
