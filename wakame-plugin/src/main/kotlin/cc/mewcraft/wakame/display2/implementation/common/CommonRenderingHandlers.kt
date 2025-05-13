// 文件说明:
// 包含了大部分渲染系统共有的 [RenderingHandler]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.display2.IndexedDataRenderer
import cc.mewcraft.wakame.display2.IndexedDataRenderer2
import cc.mewcraft.wakame.display2.IndexedDataRenderer3
import cc.mewcraft.wakame.display2.IndexedDataRenderer4
import cc.mewcraft.wakame.display2.IndexedDataRenderer5
import cc.mewcraft.wakame.display2.IndexedDataRenderer6
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingHandler
import cc.mewcraft.wakame.display2.implementation.RenderingHandler2
import cc.mewcraft.wakame.display2.implementation.RenderingHandler3
import cc.mewcraft.wakame.display2.implementation.RenderingHandler4
import cc.mewcraft.wakame.display2.implementation.RenderingHandler5
import cc.mewcraft.wakame.display2.implementation.RenderingHandler6
import cc.mewcraft.wakame.display2.implementation.RenderingHandlerRegistry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.config.property.impl.ExtraLore
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.item2.data.impl.ReforgeHistory
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 包含了大部分渲染系统共有的 [RenderingHandler] 实现.
 */
internal object CommonRenderingHandlers {
    @JvmField
    val CUSTOM_NAME: RenderingHandlerRegistry.() -> RenderingHandler<MetaCustomName, SingleValueRendererFormat> = xconfigure("custom_name") { data, format ->
        format.render(Placeholder.component("value", data.customName))
    }

    @JvmField
    val ELEMENTS: RenderingHandlerRegistry.() -> RenderingHandler<Set<RegistryEntry<Element>>, AggregateValueRendererFormat> = xconfigure("elements") { data, format ->
        format.render(data) { it.unwrap().displayName }
    }

    @JvmField
    val ITEM_NAME: RenderingHandlerRegistry.() -> RenderingHandler<MetaItemName, SingleValueRendererFormat> = xconfigure("item_name") { data, format ->
        format.render(Placeholder.parsed("value", data.itemName))
    }

    @JvmField
    val LEVEL: RenderingHandlerRegistry.() -> RenderingHandler<ItemLevel, SingleValueRendererFormat> = xconfigure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: RenderingHandlerRegistry.() -> RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = xconfigure("lore") { data, format ->
        format.render(data.lore)
    }

    @JvmField
    val RARITY: RenderingHandlerRegistry.() -> RenderingHandler2<RegistryEntry<Rarity>, ReforgeHistory, RarityRendererFormat> = xconfigure2("rarity") { data1, data2, format ->
        if (data2 == ReforgeHistory.ZERO) {
            format.renderSimple(data1)
        } else {
            format.renderComplex(data1, data2.modCount)
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
