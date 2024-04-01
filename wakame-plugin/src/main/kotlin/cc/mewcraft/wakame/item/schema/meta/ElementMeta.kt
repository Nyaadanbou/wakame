package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.random.Pool
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

typealias ElementPool = Pool<Element, SchemaGenerationContext>

/**
 * 物品的元素标识。
 */
sealed interface SElementMeta : SchemaItemMeta<Set<Element>>

private class NonNullElementMeta(
    private val elementPool: ElementPool,
) : SElementMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Set<Element>> {
        return GenerationResult(elementPool.pick(context).toSet())
    }
}

private data object DefaultElementMeta : SElementMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<Set<Element>> = GenerationResult.empty()
}

internal data object ElementMetaSerializer : SchemaItemMetaSerializer<SElementMeta> {
    override val defaultValue: SElementMeta = DefaultElementMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SElementMeta {
        return NonNullElementMeta(node.requireKt<ElementPool>())
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
 *     - value: neutral
 *       weight: 2
 *     - value: water
 *       weight: 1
 *     - value: fire
 *       weight: 1
 *     - value: wind
 *       weight: 1
 * ```
 */
internal data object ElementPoolSerializer : AbstractPoolSerializer<Element, SchemaGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): Element {
        return node.node("value").requireKt<Element>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemaGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: Element, context: SchemaGenerationContext) {
        context.elements += content
    }
}