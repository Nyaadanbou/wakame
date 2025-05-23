// 文件说明:
// 包含了大部分渲染系统共有的 [RenderingHandler]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.*
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
 * 包含了大部分渲染系统共有的 [RenderingHandler] 实现.
 */
internal object CommonRenderingHandlers {
    @JvmField
    val CUSTOM_NAME: RenderingHandlerRegistry.() -> RenderingHandler<CustomName, SingleValueRendererFormat> = xconfigure("custom_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val ELEMENTS: RenderingHandlerRegistry.() -> RenderingHandler<ItemElements, AggregateValueRendererFormat> = xconfigure("elements") { data, format ->
        format.render(data.elements) { it.unwrap().displayName }
    }

    @JvmField
    val ITEM_NAME: RenderingHandlerRegistry.() -> RenderingHandler<ItemName, SingleValueRendererFormat> = xconfigure("item_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val LEVEL: RenderingHandlerRegistry.() -> RenderingHandler<ItemLevel, SingleValueRendererFormat> = xconfigure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: RenderingHandlerRegistry.() -> RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = xconfigure("lore") { data, format ->
        format.render(data.processedLore)
    }

    @JvmField
    val RARITY: RenderingHandlerRegistry.() -> RenderingHandler2<ItemRarity, ReforgeHistory, RarityRendererFormat> = xconfigure2("rarity") { data1, data2, format ->
        if (data2 == ReforgeHistory.ZERO) {
            format.renderSimple(data1.rarity)
        } else {
            format.renderComplex(data1.rarity, data2.modCount)
        }
    }

    private inline fun <T, reified F : RendererFormat> xconfigure(id: String, block: IndexedDataRenderer<T, F>): RenderingHandlerRegistry.() -> RenderingHandler<T, F> {
        return { configure(id, block) }
    }

    private inline fun <T1, T2, reified F : RendererFormat> xconfigure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): RenderingHandlerRegistry.() -> RenderingHandler2<T1, T2, F> {
        return { configure2(id, block) }
    }

    private inline fun <T1, T2, T3, reified F : RendererFormat> xconfigure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): RenderingHandlerRegistry.() -> RenderingHandler3<T1, T2, T3, F> {
        return { configure3(id, block) }
    }

    private inline fun <T1, T2, T3, T4, reified F : RendererFormat> xconfigure4(id: String, block: IndexedDataRenderer4<T1, T2, T3, T4, F>): RenderingHandlerRegistry.() -> RenderingHandler4<T1, T2, T3, T4, F> {
        return { configure4(id, block) }
    }

    private inline fun <T1, T2, T3, T4, T5, reified F : RendererFormat> xconfigure5(id: String, block: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>): RenderingHandlerRegistry.() -> RenderingHandler5<T1, T2, T3, T4, T5, F> {
        return { configure5(id, block) }
    }

    private inline fun <T1, T2, T3, T4, T5, T6, reified F : RendererFormat> xconfigure6(id: String, block: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>): RenderingHandlerRegistry.() -> RenderingHandler6<T1, T2, T3, T4, T5, T6, F> {
        return { configure6(id, block) }
    }
}
