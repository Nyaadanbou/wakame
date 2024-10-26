package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.templates.components.*
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 包含了大部分渲染系统共有的 [RenderingPart] 实现.
 */
internal object CommonRenderingParts {
    @JvmField
    val CUSTOM_NAME: RenderingParts.() -> RenderingPart<CustomName, SingleValueRendererFormat> = xconfigure<CustomName, SingleValueRendererFormat>("custom_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val ELEMENTS: RenderingParts.() -> RenderingPart<ItemElements, AggregateValueRendererFormat> = xconfigure<ItemElements, AggregateValueRendererFormat>("elements") { data, format ->
        format.render(data.elements, Element::displayName)
    }

    @JvmField
    val ITEM_NAME: RenderingParts.() -> RenderingPart<ItemName, SingleValueRendererFormat> = xconfigure<ItemName, SingleValueRendererFormat>("item_name") { data, format ->
        format.render(Placeholder.parsed("value", data.plainName))
    }

    @JvmField
    val LEVEL: RenderingParts.() -> RenderingPart<ItemLevel, SingleValueRendererFormat> = xconfigure<ItemLevel, SingleValueRendererFormat>("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: RenderingParts.() -> RenderingPart<ExtraLore, ExtraLoreRendererFormat> = xconfigure<ExtraLore, ExtraLoreRendererFormat>("lore") { data, format ->
        format.render(data.processedLore)
    }

    @JvmField
    val RARITY: RenderingParts.() -> RenderingPart<ItemRarity, SingleValueRendererFormat> = xconfigure<ItemRarity, SingleValueRendererFormat>("rarity") { data, format ->
        format.render(Placeholder.component("value", data.rarity.displayName))
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
