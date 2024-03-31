package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.random.Pool
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

typealias ElementPool = Pool<Element, SchemeGenerationContext>

/**
 * 物品的元素标识。
 */
sealed interface SElementMeta : SchemeItemMeta<Set<Element>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "element")
    }
}

private class NonNullElementMeta(
    private val elementPool: ElementPool,
) : SElementMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<Set<Element>> {
        return GenerationResult(elementPool.pick(context).toSet())
    }
}

private data object DefaultElementMeta : SElementMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<Set<Element>> = GenerationResult.empty()
}

internal class ElementMetaSerializer : SchemeItemMetaSerializer<SElementMeta> {
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
internal class ElementPoolSerializer : AbstractPoolSerializer<Element, SchemeGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): Element {
        return node.node("value").requireKt<Element>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: Element, context: SchemeGenerationContext) {
        context.elements += content
    }
}