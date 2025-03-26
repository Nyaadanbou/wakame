package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.util.require
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 代表从 [EntityDamageEvent] 创建 [DamageMetadata] 的逻辑.
 */
interface DamageMapper {

    /**
     * 生成一个反映此映射的 [DamageMetadata] 实例.
     */
    fun generate(event: EntityDamageEvent): DamageMetadata

}

/**
 * 一个特定攻击场景的伤害映射.
 *
 * @param tests 用于检查场景是否匹配
 * @param builder 用于构建 [DamageMetadata]
 */
internal data class DamagePredicateMapper(
    val tests: List<DamagePredicate>,
    val builder: DamageMetadataBuilder<*>,
) : DamageMapper {

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<DamagePredicateMapper> = DamageMapperSerializer
    }

    /**
     * 检查传入的 [event] 是否与此映射相匹配.
     */
    fun match(event: EntityDamageEvent): Boolean {
        return tests.all { it.test(event) }
    }

    override fun generate(event: EntityDamageEvent): DamageMetadata {
        return builder.build(event)
    }

}

private object DamageMapperSerializer : TypeSerializer<DamagePredicateMapper> {

    override fun deserialize(type: Type, node: ConfigurationNode): DamagePredicateMapper {
        val predicates = node.node("predicates").childrenMap()
            // 顺序在这里有作用
            .map { (_, mapValue) ->
                mapValue.require<DamagePredicate>()
            }
        val builder = node.node("damage_metadata").require<DamageMetadataBuilder<*>>()
        return DamagePredicateMapper(predicates, builder)
    }

}