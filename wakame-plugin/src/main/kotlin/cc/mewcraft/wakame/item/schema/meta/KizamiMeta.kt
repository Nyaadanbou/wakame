package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.filter.FilterFactory
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.random.Pool
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

typealias KizamiPool = Pool<Kizami, SchemaGenerationContext>
typealias KizamiGroup = Group<Kizami, SchemaGenerationContext>

/**
 * 物品的铭刻标识。
 */
sealed interface SKizamiMeta : SchemaItemMeta<Set<Kizami>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "kizami")
    }
}

private class NonNullKizamiMeta(
    private val kizamiGroup: KizamiGroup,
) : SKizamiMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Set<Kizami>> {
        val value = kizamiGroup.pick(context).toSet()
        return if (value.isNotEmpty()) {
            GenerationResult(value)
        } else {
            GenerationResult.empty()
        }
    }
}

private data object DefaultKizamiMeta : SKizamiMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Set<Kizami>> = GenerationResult.empty()
}

internal class KizamiMetaSerializer : SchemaItemMetaSerializer<SKizamiMeta> {
    override val defaultValue: SKizamiMeta = DefaultKizamiMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SKizamiMeta {
        return NonNullKizamiMeta(node.requireKt<KizamiGroup>())
    }
}

/**
 * @see AbstractGroupSerializer
 */
internal class KizamiGroupSerializer : AbstractGroupSerializer<Kizami, SchemaGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<Kizami, SchemaGenerationContext> {
        return node.requireKt<KizamiPool>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - value: wood
 *       weight: 3
 *     - value: iron
 *       weight: 2
 *     - value: gold
 *       weight: 1
 *     - value: diamond
 *       weight: 1
 *     - value: netherite
 *       weight: 1
 * ```
 */
internal class KizamiPoolSerializer : AbstractPoolSerializer<Kizami, SchemaGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): Kizami {
        return node.node("value").requireKt<Kizami>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: Kizami, context: SchemaGenerationContext) {
        context.kizamis += content
    }
}