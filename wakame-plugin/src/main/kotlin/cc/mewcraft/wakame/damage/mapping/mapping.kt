package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.damage.RawDamageContext
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * 代表一个从 [RawDamageContext] 创建 [DamageMetadata] 的逻辑.
 */
internal interface DamageMapper {

    /**
     * 生成一个反映 [context] 的 [DamageMetadata].
     */
    fun generate(context: RawDamageContext): DamageMetadata

}

/**
 * 用于生成 [org.bukkit.damage.DamageType] 对应的 [DamageMetadata].
 */
@ConfigSerializable
internal data class DamageTypeMapper(
    @Required @Setting("damage_metadata")
    val builder: DamageMetadataBuilder<*>,
) : DamageMapper {
    override fun generate(context: RawDamageContext): DamageMetadata {
        return builder.build(context)
    }
}

/**
 * 一个特定攻击场景的伤害映射. 攻击场景的检查由 [tests] 负责.
 *
 * @param tests 用于检查场景是否匹配
 * @param builder 用于构建 [DamageMetadata]
 */
internal data class DamagePredicateMapper(
    val tests: List<DamagePredicate>,
    val builder: DamageMetadataBuilder<*>,
) : DamageMapper {

    companion object {
        fun serializer(): TypeSerializer<DamagePredicateMapper> = SimpleSerializer { _: Type, node: ConfigurationNode ->
            val tests = node.node("predicates").childrenMap().values.map { it.require<DamagePredicate>() }
            val builder = node.node("damage_metadata").require<DamageMetadataBuilder<*>>()
            DamagePredicateMapper(tests, builder)
        }
    }

    /**
     * 检查传入的 [context] 是否与此映射相匹配.
     */
    fun match(context: RawDamageContext): Boolean {
        return tests.all { it.test(context) }
    }

    override fun generate(context: RawDamageContext): DamageMetadata {
        return builder.build(context)
    }
}
