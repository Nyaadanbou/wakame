/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.nio.file.Path
import kotlin.properties.Delegates

internal class StandardRendererLayout : AbstractRendererLayout() {
    override var staticIndexedTextList: List<IndexedText> = ArrayList() // TODO display2
    override var defaultIndexedTextList: List<IndexedText> = ArrayList() // TODO display2
}

internal class StandardRendererFormats : AbstractRendererFormats() {
    override fun <T : RendererFormat> get(id: String): T? {
        TODO("display2")
    }

    override fun <T : RendererFormat> set(id: String, format: T) {
        TODO("display2")
    }
}

internal class StandardContext

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>(), KoinComponent {
    override var rendererLayout: RendererLayout by Delegates.notNull()
    override var rendererFormats: RendererFormats by Delegates.notNull()
    private var indexedLineFlatter: IndexedTextFlatter by Delegates.notNull()

    override fun initialize(
        layoutPath: Path,
        formatPath: Path,
    ) {
        // TODO 读取配置文件, 初始化:
        //   rendererLayout, rendererFormats
        rendererLayout = StandardRendererLayout() // TODO display2
        rendererFormats = StandardRendererFormats() // TODO display2
        indexedLineFlatter = IndexedTextFlatter(rendererLayout)
    }

    override fun render(item: PacketNekoStack, context: StandardContext?) {
        val components = item.components
        val collector = ObjectArrayList<IndexedText>()

        components.process(ItemComponentTypes.ATTACK_SPEED) { data -> StandardRenderingParts.ATTACK_SPEED.process(collector, data) }
        components.process(ItemComponentTypes.CELLS) { data ->
            for ((_, cell) in data) {
                when (
                    val core = cell.getCore()
                ) {
                    is AttributeCore -> StandardRenderingParts.CELLULAR_ATTRIBUTE.process(collector, core.attribute)
                    is SkillCore -> StandardRenderingParts.CELLULAR_SKILL.process(collector, core.skill)
                    else -> StandardRenderingParts.CELLULAR_EMPTY.process(collector, null)
                }
            }
        }
        components.process(ItemComponentTypes.CRATE) { data -> StandardRenderingParts.CRATE.process(collector, data) }
        components.process(ItemComponentTypes.CUSTOM_NAME) { data -> StandardRenderingParts.CUSTOM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.ELEMENTS) { data -> StandardRenderingParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> StandardRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FIRE_RESISTANT) { data -> StandardRenderingParts.FIRE_RESISTANT.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> StandardRenderingParts.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.ITEM_NAME) { data -> StandardRenderingParts.ITEM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.KIZAMIZ) { data -> StandardRenderingParts.KIZAMIZ.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> StandardRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.LORE) { data -> StandardRenderingParts.LORE.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> StandardRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.RARITY) { data -> StandardRenderingParts.RARITY.process(collector, data) }

        val lore = indexedLineFlatter.flatten(collector)
        val cmd = ItemModelDataLookup[item.id, item.variant]

        // 修改物品(原地)
        item.lore(lore)
        item.customModelData(cmd)
    }

    private inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }
}

/**
 * 用于(反)序列化渲染器的配置文件.
 */
internal object StandardRendererConfigSerializer {

}


//////


//<editor-fold desc="RenderingPart">
internal object StandardRenderingParts {
    @JvmField
    val ATTACK_SPEED: StandardRenderingPart<ItemAttackSpeed, SingleValueRendererFormat> = configure("attack_speed") { data, format ->
        val level = data.level
        val text = format.render(Placeholder.component("value", Component.text(level.name)))
        listOf(SimpleIndexedText(format.index, listOf(text)))
    }

    @JvmField
    val CELLULAR_ATTRIBUTE: StandardRenderingPart<ConstantCompositeAttribute, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        val text = format.render(data)
        val idx = format.computeIndex(data)
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val CELLULAR_SKILL: StandardRenderingPart<ConfiguredSkill, CellularSkillRendererFormat> = configure("cells/skills") { data, format ->
        val text = format.render(data)
        val idx = format.computeIndex(data)
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val CELLULAR_EMPTY: StandardRenderingPart<Nothing?, CellularEmptyRendererFormat> = configure("cells/empty") { _, format ->
        val text = format.render()
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val CRATE: StandardRenderingPart<ItemCrate, AggregateValueRendererFormat> = configure("crate") { data, format ->
        emptyList() // TODO display2
    }

    @JvmField
    val CUSTOM_NAME: StandardRenderingPart<CustomName, SingleValueRendererFormat> = configure("custom_name") { data, format ->
        val text = format.render(Placeholder.component("value", data.rich))
        val idx = format.index
        listOf(SimpleIndexedText(idx, listOf(text)))
    }

    @JvmField
    val ELEMENTS: StandardRenderingPart<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        val text = format.render(data.elements, Element::displayName)
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val ENCHANTMENTS: StandardRenderingPart<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        val text = format.render(data.enchantments)
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val FIRE_RESISTANT: StandardRenderingPart<FireResistant, SingleValueRendererFormat> = configure("fire_resistant") { _, format ->
        val text = format.render()
        val idx = format.index
        listOf(SimpleIndexedText(idx, listOf(text)))
    }

    @JvmField
    val FOOD: StandardRenderingPart<FoodProperties, FoodPropertiesRendererFormat> = configure("food") { data, format ->
        val text = format.render(data)
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val ITEM_NAME: StandardRenderingPart<ItemName, SingleValueRendererFormat> = configure("item_name") { data, format ->
        val text = format.render(Placeholder.component("value", data.rich))
        val idx = format.index
        listOf(SimpleIndexedText(idx, listOf(text)))
    }

    @JvmField
    val KIZAMIZ: StandardRenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        val text = format.render(data.kizamiz, Kizami::displayName)
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val LEVEL: StandardRenderingPart<ItemLevel, SingleValueRendererFormat> = configure("level") { data, format ->
        val text = format.render(Placeholder.component("value", Component.text(data.level)))
        val idx = format.index
        listOf(SimpleIndexedText(idx, listOf(text)))
    }

    @JvmField
    val LORE: StandardRenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        val text = format.render(data.lore)
        val idx = format.index
        listOf(SimpleIndexedText(idx, text))
    }

    @JvmField
    val PORTABLE_CORE: StandardRenderingPart<PortableCore, SingleValueRendererFormat> = configure("portable_core") { data, format ->
        emptyList() // TODO display2 // 把核心的渲染逻辑分离出来, 不仅可以在这里 (PortableCore) 使用, 还可以在 ItemCells 使用
    }

    @JvmField
    val RARITY: StandardRenderingPart<ItemRarity, SingleValueRendererFormat> = configure("rarity") { data, format ->
        val text = format.render(Placeholder.component("value", data.rarity.displayName))
        val idx = format.index
        listOf(SimpleIndexedText(idx, listOf(text)))
    }

    private inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): StandardRenderingPart<T, F> {
        TODO("display2")
    }

    private inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): StandardRenderingPart2<T1, T2, F> {
        TODO("display2")
    }

    private inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): StandardRenderingPart3<T1, T2, T3, F> {
        TODO("display2")
    }
}

/**
 * 聚合了渲染一种物品数据所需要的数据和逻辑.
 *
 * @param T 被渲染的数据类型
 * @param F 渲染格式的类型
 */
internal class StandardRenderingPart<T, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer<T, F>,
) : RenderingPart<T, F> {
    fun process(collector: MutableList<IndexedText>, data: T) {
        collector += renderer.render(data, format)
    }
}

internal class StandardRenderingPart2<T1, T2, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer2<T1, T2, F>,
) : RenderingPart2<T1, T2, F> {
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2) {
        collector += renderer.render(data1, data2, format)
    }
}

internal class StandardRenderingPart3<T1, T2, T3, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) : RenderingPart3<T1, T2, T3, F> {
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3) {
        collector += renderer.render(data1, data2, data3, format)
    }
}
//</editor-fold>


//////


//<editor-fold desc="RendererFormat">
internal class CellularAttributeRendererFormat(
    override val namespace: String,
) : RendererFormat.Dynamic<ConstantCompositeAttribute> {
    override fun computeIndex(source: ConstantCompositeAttribute): Key {
        TODO("display2")
    }

    fun render(attribute: ConstantCompositeAttribute): List<Component> {
        val facade = AttributeRegistry.FACADES[attribute.id]
        return facade.createTooltipLore(attribute)
    }
}

internal class CellularSkillRendererFormat(
    override val namespace: String,
) : RendererFormat.Dynamic<ConfiguredSkill>, KoinComponent {
    private val miniMessage = get<MiniMessage>()

    override fun computeIndex(source: ConfiguredSkill): Key {
        val sourceId = source.id
        return Key.key(namespace, sourceId.namespace() + "/" + sourceId.key())
    }

    fun render(skill: ConfiguredSkill): List<Component> {
        val skillObject = skill.instance
        return skillObject.displays.tooltips.map(miniMessage::deserialize)
    }
}

internal class CellularEmptyRendererFormat(
    override val namespace: String,
    private val fallback: List<Component>,
) : RendererFormat.Simple {
    override val index: Key = Key.key(namespace, "cells/empty")

    fun render(): List<Component> {
        return fallback
    }
}

internal class FoodPropertiesRendererFormat(
    override val namespace: String,
    private val lines: List<String>,
) : RendererFormat.Simple {
    override val index: Key = Key.key(namespace, "food")

    fun render(food: FoodProperties): List<Component> {
        return emptyList() // TODO display2
    }
}
//</editor-fold>
