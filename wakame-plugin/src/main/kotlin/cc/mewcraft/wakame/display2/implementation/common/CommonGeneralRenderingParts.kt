// 文件说明:
// 包含了大部分渲染系统共有的 [RenderingPart]

package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedDataRenderer
import cc.mewcraft.wakame.display2.IndexedDataRenderer2
import cc.mewcraft.wakame.display2.IndexedDataRenderer3
import cc.mewcraft.wakame.display2.IndexedDataRenderer4
import cc.mewcraft.wakame.display2.IndexedDataRenderer5
import cc.mewcraft.wakame.display2.IndexedDataRenderer6
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart2
import cc.mewcraft.wakame.display2.implementation.RenderingPart3
import cc.mewcraft.wakame.display2.implementation.RenderingPart4
import cc.mewcraft.wakame.display2.implementation.RenderingPart5
import cc.mewcraft.wakame.display2.implementation.RenderingPart6
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.rarity.Rarity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

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

@ConfigSerializable
data class RarityRendererFormat(
    override val namespace: String,
    private val simple: String,
    private val complex: String,
) : RendererFormat.Simple {
    override val id: String = "rarity"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    fun renderSimple(rarity: Rarity): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    simple,
                    Placeholder.component("rarity_display_name", rarity.displayName)
                )
            )
        )
    }

    fun renderComplex(rarity: Rarity, modCount: Int): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    complex,
                    Placeholder.component("rarity_display_name", rarity.displayName),
                    Placeholder.component("reforge_mod_count", Component.text(modCount.toString()))
                )
            )
        )
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: SingleSimpleTextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = (data.wrapped as? AttributeCore)
            ?: return SimpleIndexedText(unknownIndex, emptyList())
        return SimpleIndexedText(index, core.description)
    }
}