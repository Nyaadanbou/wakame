@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.wakame.damage.DamageContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.require
import org.bukkit.damage.DamageType
import org.bukkit.entity.*
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 用于检查 [DamageContext] 是否属于一个特定场景的谓词.
 */
internal sealed interface DamagePredicate {

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<DamagePredicate> = Serializer
    }

    fun test(context: DamageContext): Boolean

    //

    private object Serializer : TypeSerializer2<DamagePredicate> {
        override fun deserialize(type: Type, node: ConfigurationNode): DamagePredicate {
            // FIXME 使用 DispatchingTypeSerializer 替代
            // FIXME 这里没有使用单独的 Node 来指定 type, 而是用的 Node 本身的 key 来指定 type
            //  截止 1/26 DispatchingTypeSerializer 仅支持在单独的 Node 上指定 type
            //  新的 DispatchingTypeSerializer 实现应该支持这种 “inline” type
            return when (val key = node.key().toString()) {
                EntityDataPredicate.TYPE_ID -> {
                    val map = node.childrenMap()
                        .transformKeys<String>()
                        .mapValues { (_, nodeValue) -> nodeValue.require<Int>() }
                    EntityDataPredicate(map)
                }

                DamageTypePredicate.TYPE_ID -> {
                    val damageTypes = node.getList<DamageType>(emptyList())
                    DamageTypePredicate(damageTypes.toHashSet())
                }

                CausingEntityTypePredicate.TYPE_ID -> {
                    val entityTypes = node.getList<EntityType>(emptyList())
                    CausingEntityTypePredicate(entityTypes.toHashSet())
                }

                DirectEntityTypePredicate.TYPE_ID -> {
                    val entityTypes = node.getList<EntityType>(emptyList())
                    DirectEntityTypePredicate(entityTypes.toHashSet())
                }

                VictimEntityTypePredicate.TYPE_ID -> {
                    val entityTypes = node.getList<EntityType>(emptyList())
                    VictimEntityTypePredicate(entityTypes.toHashSet())
                }

                else -> {
                    throw SerializationException("Unknown damage predicate type: '$key'")
                }
            }
        }
    }

}

/**
 * 检查攻击实体某个特定数据的谓词.
 * 目前支持检查的数据在伴生类中列举.
 */
private data class EntityDataPredicate(
    val requiredData: Map<String, Int>,
) : DamagePredicate {
    companion object {
        const val TYPE_ID = "entity_data"

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

    override fun test(context: DamageContext): Boolean {
        val damager = context.damageSource.causingEntity as? LivingEntity ?: return false
        return requiredData.all { (str, num) -> map[str]?.invoke(num, damager) == true }
    }
}

/**
 * 检查伤害类型的谓词.
 */
private data class DamageTypePredicate(
    val types: Set<DamageType>,
) : DamagePredicate {
    companion object {
        const val TYPE_ID = "damage_type"
    }

    override fun test(context: DamageContext): Boolean {
        return context.damageSource.damageType in types
    }
}

/**
 * 检查来源实体类型的谓词.
 */
private data class CausingEntityTypePredicate(
    val types: Set<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_ID = "causing_entity_type"
    }

    override fun test(context: DamageContext): Boolean {
        val causingEntity = context.damageSource.causingEntity ?: return false
        return causingEntity.type in types
    }
}

/**
 * 检查直接实体类型的谓词.
 */
private data class DirectEntityTypePredicate(
    val types: Set<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_ID = "direct_entity_type"
    }

    override fun test(context: DamageContext): Boolean {
        val directEntity = context.damageSource.directEntity ?: return false
        return directEntity.type in types
    }
}

/**
 * 检查受伤实体类型的谓词.
 */
private data class VictimEntityTypePredicate(
    val types: Set<EntityType>,
) : DamagePredicate {
    companion object {
        const val TYPE_ID = "victim_entity_type"
    }

    override fun test(context: DamageContext): Boolean {
        return context.damagee.type in types
    }
}
