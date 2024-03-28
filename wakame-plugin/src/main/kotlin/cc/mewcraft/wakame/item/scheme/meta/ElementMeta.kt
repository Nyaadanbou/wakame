package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
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
sealed interface ElementMeta : SchemeItemMeta<Set<Element>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "element")
    }
}

private class NonNullElementMeta(
    private val elementPool: ElementPool,
) : ElementMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Set<Element>> {
        return elementPool.pick(context).toSet().toMetaResult()
    }
}

private data object DefaultElementMeta : ElementMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<Set<Element>> = nonGenerate()
}

internal class ElementMetaSerializer : SchemeItemMetaSerializer<ElementMeta> {
    override val defaultValue: ElementMeta = DefaultElementMeta

    override fun deserialize(type: Type, node: ConfigurationNode): ElementMeta {
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