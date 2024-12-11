// 文件说明:
// 包含了大部分渲染系统共有的 [RenderingPart]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.display2.IndexedDataRenderer
import cc.mewcraft.wakame.display2.IndexedDataRenderer2
import cc.mewcraft.wakame.display2.IndexedDataRenderer3
import cc.mewcraft.wakame.display2.IndexedDataRenderer4
import cc.mewcraft.wakame.display2.IndexedDataRenderer5
import cc.mewcraft.wakame.display2.IndexedDataRenderer6
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart2
import cc.mewcraft.wakame.display2.implementation.RenderingPart3
import cc.mewcraft.wakame.display2.implementation.RenderingPart4
import cc.mewcraft.wakame.display2.implementation.RenderingPart5
import cc.mewcraft.wakame.display2.implementation.RenderingPart6
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.ItemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 包含了大部分渲染系统共有的 [RenderingPart] 实现.
 */
internal object CommonRenderingParts {
    @JvmField
    val CUSTOM_NAME: RenderingParts.() -> RenderingPart<CustomName, SingleValueRendererFormat> = xconfigure("custom_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val ELEMENTS: RenderingParts.() -> RenderingPart<ItemElements, AggregateValueRendererFormat> = xconfigure("elements") { data, format ->
        format.render(data.elements, Element::displayName)
    }

    @JvmField
    val ITEM_NAME: RenderingParts.() -> RenderingPart<ItemName, SingleValueRendererFormat> = xconfigure("item_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val LEVEL: RenderingParts.() -> RenderingPart<ItemLevel, SingleValueRendererFormat> = xconfigure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: RenderingParts.() -> RenderingPart<ExtraLore, ExtraLoreRendererFormat> = xconfigure("lore") { data, format ->
        format.render(data.processedLore)
    }

    @JvmField
    val RARITY: RenderingParts.() -> RenderingPart2<ItemRarity, ReforgeHistory, RarityRendererFormat> = xconfigure2("rarity") { data1, data2, format ->
        if (data2 == ReforgeHistory.ZERO) {
            format.renderSimple(data1.rarity)
        } else {
            format.renderComplex(data1.rarity, data2.modCount)
        }
    }

    private inline fun <T, reified F : RendererFormat> xconfigure(id: String, block: IndexedDataRenderer<T, F>): RenderingParts.() -> RenderingPart<T, F> {
        return { configure(id, block) }
    }

    private inline fun <T1, T2, reified F : RendererFormat> xconfigure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): RenderingParts.() -> RenderingPart2<T1, T2, F> {
        return { configure2(id, block) }
    }

    private inline fun <T1, T2, T3, reified F : RendererFormat> xconfigure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): RenderingParts.() -> RenderingPart3<T1, T2, T3, F> {
        return { configure3(id, block) }
    }

    private inline fun <T1, T2, T3, T4, reified F : RendererFormat> xconfigure4(id: String, block: IndexedDataRenderer4<T1, T2, T3, T4, F>): RenderingParts.() -> RenderingPart4<T1, T2, T3, T4, F> {
        return { configure4(id, block) }
    }

    private inline fun <T1, T2, T3, T4, T5, reified F : RendererFormat> xconfigure5(id: String, block: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>): RenderingParts.() -> RenderingPart5<T1, T2, T3, T4, T5, F> {
        return { configure5(id, block) }
    }

    private inline fun <T1, T2, T3, T4, T5, T6, reified F : RendererFormat> xconfigure6(id: String, block: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>): RenderingParts.() -> RenderingPart6<T1, T2, T3, T4, T5, T6, F> {
        return { configure6(id, block) }
    }
}
