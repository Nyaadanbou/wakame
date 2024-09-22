/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.DataComponentRenderer
import cc.mewcraft.wakame.display2.DerivedTooltipIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.IndexedTextFlatter
import cc.mewcraft.wakame.display2.ItemRenderer
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ExtraLore
import cc.mewcraft.wakame.item.components.ItemAttackSpeed
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.KoinComponent
import java.nio.file.Path

internal class StandardRendererLayout : AbstractRendererLayout() {
    override var staticIndexedTextList: List<IndexedText> = ArrayList()
    override var defaultIndexedTextList: List<IndexedText> = ArrayList()
}

internal class StandardRendererFormats : AbstractRendererFormats() {
    override fun <T : RendererFormat> get(id: String): T? {
        TODO("Not yet implemented")
    }

    override fun <T : RendererFormat> set(id: String, format: T) {
        TODO("Not yet implemented")
    }
}

internal object StandardItemRenderer : ItemRenderer<PacketNekoStack, Nothing>, KoinComponent {
    override var rendererLayout: RendererLayout
        get() = TODO()
        set(value) {}

    override var rendererFormats: RendererFormats
        get() = TODO()
        set(value) {}

    private var indexedLineFlatter: IndexedTextFlatter
        get() = TODO()
        set(value) {}

    override fun initialize(
        layoutPath: Path,
        formatPath: Path
    ) {
        // TODO 读取配置文件, 初始化:
        //   rendererLayout, rendererFormats, indexedLineFlatter
        rendererLayout = TODO()
        rendererFormats = TODO()
        indexedLineFlatter = IndexedTextFlatter(rendererLayout)
        standardRenderingUnits = createStandardRenderingUnits()
    }

    override fun render(item: PacketNekoStack, context: Nothing?) {
        val components = item.components
        val unprocessed = ObjectArrayList<IndexedText>()
        val renderingUnits = standardRenderingUnits

        fun <T> process(componentType: ItemComponentType<T>) {
            val unit = renderingUnits[componentType]
            unit.process(unprocessed, components)
        }

        process(ItemComponentTypes.ATTACK_SPEED)
        process(ItemComponentTypes.CELLS)
        process(ItemComponentTypes.ELEMENTS)
        process(ItemComponentTypes.ENCHANTMENTS)
        process(ItemComponentTypes.KIZAMIZ)
        process(ItemComponentTypes.LEVEL)
        process(ItemComponentTypes.LORE)
        process(ItemComponentTypes.PORTABLE_CORE)
        process(ItemComponentTypes.RARITY)

        val lore = indexedLineFlatter.flatten(unprocessed)
        val cmd = ItemModelDataLookup[item.key, item.variant]

        // 修改物品(原地)
        item.lore(lore)
        item.customModelData(cmd)
    }

    // TODO 为什么不把每个 renderer 的实现直接作为这个 ItemRenderer 的成员?
    //  这样直接省去了 hash 函数的开销, 写起来,引用起来, 也更加直接
    //  不能这样! 因为这要求每个对象支持 reload.
    //  而如果用 map 这种容器的话, reload 的时候只需要重新构建 map 即可.

    private var standardRenderingUnits: RenderingUnitMap = createStandardRenderingUnits()
    private fun createStandardRenderingUnits(): RenderingUnitMap = RenderingUnitMap(this) {
        define<ItemAttackSpeed, SingleValueRendererFormat>(ItemComponentTypes.ATTACK_SPEED) { data, format ->
            val level = data.level
            val text = format.render(Placeholder.component("value", Component.text(level.name)))
            listOf(SimpleIndexedText(format.index, listOf(text)))
        }

        define<ItemCells, ItemCellsRendererFormat>(ItemComponentTypes.CELLS) { data, format ->
            // TODO 词条栏的渲染逻辑很复杂, 需要分离出它自己的逻辑
            format.render(data)
        }

        define<ItemElements, CollectionRendererFormat>(ItemComponentTypes.ELEMENTS) { data, format ->
            val elements = data.elements
            val text = format.render(elements, Element::displayName)
            listOf(SimpleIndexedText(format.index, text))
        }

        define<ItemEnchantments, ItemEnchantmentsRendererFormat>(ItemComponentTypes.ENCHANTMENTS) { data, format ->
            // TODO 附魔的渲染逻辑较为复杂, 需要分离出它自己的逻辑
            format.render(data)
        }

        define<ItemKizamiz, CollectionRendererFormat>(ItemComponentTypes.KIZAMIZ) { data, format ->
            val kizamiz = data.kizamiz
            val text = format.render(kizamiz, Kizami::displayName)
            listOf(SimpleIndexedText(format.index, text))
        }

        define<ItemLevel, SingleValueRendererFormat>(ItemComponentTypes.LEVEL) { data, format ->
            val level = data.level
            val text = format.render(Placeholder.component("value", Component.text(level)))
            listOf(SimpleIndexedText(format.index, listOf(text)))
        }

        define<ItemRarity, SingleValueRendererFormat>(ItemComponentTypes.RARITY) { data, format ->
            val rarity = data.rarity
            val text = format.render(Placeholder.component("value", rarity.displayName))
            listOf(SimpleIndexedText(format.index, listOf(text)))
        }

        define<ExtraLore, CollectionRendererFormat>(ItemComponentTypes.LORE) { data, format ->
            val lore = data.lore
            val text = format.render(lore, String::mini)
            listOf(SimpleIndexedText(format.index, text))
        }

        define<PortableCore, SingleValueRendererFormat>(ItemComponentTypes.PORTABLE_CORE) { data, format ->
            // TODO 把核心的渲染逻辑分离出来, 不仅可以在这里 (PortableCore) 使用, 还可以在 ItemCells 使用
            val text = format.render(Placeholder.component("value", TODO()))
            listOf(SimpleIndexedText(format.index, listOf(text)))
        }

        // TODO add more DataComponentRenderer ...
    }


    //////


    /**
     * 聚合了渲染一种物品数据所需要的数据和逻辑.
     *
     * @param T 被渲染的数据类型
     * @param F 渲染格式的类型
     */
    interface RenderingUnit<T, F : RendererFormat> {
        val rendererFormat: F
        val componentType: ItemComponentType<T>
        val dataRenderer: DataComponentRenderer<T, F>

        /**
         * @param collector 用于收集渲染结果的容器
         * @param components 存放了物品所有组件的容器
         */
        fun process(collector: MutableList<IndexedText>, components: ItemComponentMap)
    }

    object EmptyRenderingUnit : RenderingUnit<Nothing, Nothing> {
        override val rendererFormat: Nothing
            get() = error("EmptyRenderingUnit does not have a rendererFormat.")
        override val componentType: Nothing
            get() = error("EmptyRenderingUnit does not have a componentType.")
        override val dataRenderer: Nothing
            get() = error("EmptyRenderingUnit does not have a dataRenderer.")

        override fun process(collector: MutableList<IndexedText>, components: ItemComponentMap) = Unit
    }

    class SimpleRenderingUnit<T, F : RendererFormat>(
        override val rendererFormat: F,
        override val componentType: ItemComponentType<T>,
        override val dataRenderer: DataComponentRenderer<T, F>
    ) : RenderingUnit<T, F> {
        override fun process(collector: MutableList<IndexedText>, components: ItemComponentMap) {
            val component = components.get(componentType) ?: return // 如果没有这个组件, 则不渲染
            val rendered = dataRenderer.render(component, rendererFormat)
            collector += rendered
        }
    }


    //////


    fun <S, C> RenderingUnitMap(
        renderer: ItemRenderer<S, C>,
        block: RenderingUnitMapDSL<S, C>.() -> Unit
    ): RenderingUnitMap {
        return RenderingUnitMapDSL<S, C>(renderer).apply(block).build()
    }

    /**
     * 用于构建 [RenderingUnitMap].
     *
     * @param S 物品的类型
     * @param C 上下文的类型
     */
    class RenderingUnitMapDSL<in S, in C>(
        private val renderer: ItemRenderer<S, C>,
    ) {
        private val data = Reference2ReferenceOpenHashMap<ItemComponentType<*>, RenderingUnit<*, *>>()
        private val rendererLayout = renderer.rendererLayout // 重新赋值, 避免 partial reads
        private val rendererFormats = renderer.rendererFormats // 重新赋值, 避免 partial reads

        @Suppress("UNCHECKED_CAST")
        fun <T, F : RendererFormat> define(
            componentType: ItemComponentType<T>,
            renderingLogic: (T, F) -> Collection<IndexedText>,
        ): RenderingUnit<T, F> {
            val id = componentType.id
            val format = rendererFormats.get<F>(id) ?: return EmptyRenderingUnit as RenderingUnit<T, F>
            val renderer = DataComponentRenderer<T, F> { data, format -> renderingLogic(data, format) }
            return SimpleRenderingUnit(format, componentType, renderer)
        }

        fun build(): RenderingUnitMap {
            return RenderingUnitMap(data)
        }
    }

    class RenderingUnitMap(
        map: Map<ItemComponentType<*>, RenderingUnit<*, *>>,
    ) {
        private val data = Reference2ReferenceOpenHashMap<ItemComponentType<*>, RenderingUnit<*, *>>(map)

        operator fun <T> get(
            componentType: ItemComponentType<T>
        ): RenderingUnit<T, *> {
            @Suppress("UNCHECKED_CAST")
            return data[componentType] as RenderingUnit<T, *>
        }
    }
}

/**
 * 用于(反)序列化渲染器的配置文件.
 */
object StandardRendererConfigSerializer {

}


//////


//<editor-fold desc="RendererFormat">
/**
 * @see Core
 */
private class ItemCoresRendererFormat(
    override val index: DerivedTooltipIndex
) : RendererFormat {
    fun render(core: Core): Collection<IndexedText> {
        return emptyList()
    }
}

/**
 * @see ItemCells
 */
private class ItemCellsRendererFormat(
    override val index: DerivedTooltipIndex
) : RendererFormat {
    fun render(cells: ItemCells): Collection<IndexedText> {
        return emptyList()
    }
}

/**
 * @see ItemEnchantments
 */
private class ItemEnchantmentsRendererFormat(
    override val index: DerivedTooltipIndex
) : RendererFormat, KoinComponent {
    fun render(enchantments: ItemEnchantments): Collection<IndexedText> {
        return emptyList()
    }
}
//</editor-fold>

