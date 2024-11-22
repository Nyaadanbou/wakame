package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.util.krequire
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 一个特定攻击场景的伤害映射.
 * [predicates] 用于检查场景是否匹配.
 * 使用 [builder] 创建 [DamageMetadata].
 */
data class DamageMapping(
    val predicates: List<DamagePredicate>,
    val builder: DamageMetadataBuilder<*>,
) {

    /**
     * 检查传入的 [event] 是否与此映射相匹配.
     */
    fun match(event: EntityDamageEvent): Boolean {
        return predicates.all { it.test(event) }
    }

    /**
     * 生成一个反映了此映射的 [DamageMetadata] 实例.
     */
    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return builder.build(event)
    }
}

/**
 * [DamageMapping] 的序列化器.
 */
internal object DamageMappingSerializer : TypeSerializer<DamageMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): DamageMapping {
        val predicates = node.node("predicates").childrenMap()
            // 顺序在这里有作用
            .map { (_, mapValue) ->
                mapValue.krequire<DamagePredicate>()
            }
        val builder = node.node("damage_metadata").krequire<DamageMetadataBuilder<*>>()
        return DamageMapping(predicates, builder)
    }
}