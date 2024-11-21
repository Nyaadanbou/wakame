package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.util.krequire
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * 一个特定攻击场景的伤害映射.
 * [predicates] 检查场景.
 * [damageMetadataBuilder] 取得 [DamageMetadata].
 */
data class DamageMapping(
    val predicates: List<DamagePredicate>,
    val damageMetadataBuilder: DamageMetadataBuilder<*>
) {

    /**
     * 检查传入的 [event] 是否与此映射相匹配.
     */
    fun match(event: EntityDamageEvent): Boolean {
        predicates.forEach {
            if (!it.test(event)) return false
        }
        return true
    }

    /**
     * 生成一个反映了此映射的 [DamageMetadata] 实例.
     */
    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataBuilder.build(event)
    }
}

/**
 * [DamageMapping] 的序列化器.
 */
internal object DamageMappingSerializer : TypeSerializer<DamageMapping> {

    override fun deserialize(type: Type, node: ConfigurationNode): DamageMapping {
        val predicates = node.node("predicates").childrenMap()
            .map { (_, mapValue) ->
                mapValue.krequire<DamagePredicate>()
            }
        val damageMetadataBuilder = node.node("damage_metadata").krequire<DamageMetadataBuilder<*>>()
        return DamageMapping(predicates, damageMetadataBuilder)
    }

    override fun serialize(type: Type, obj: DamageMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}