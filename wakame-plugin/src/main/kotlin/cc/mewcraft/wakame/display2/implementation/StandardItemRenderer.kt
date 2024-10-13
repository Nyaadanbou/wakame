/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.composite.*
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.util.StringCombiner
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path
import kotlin.reflect.typeOf

internal class StandardRendererFormats : AbstractRendererFormats()

internal class StandardRendererLayout(rendererFormats: AbstractRendererFormats) : AbstractRendererLayout(rendererFormats)

internal class StandardContext

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>() {
    override val rendererFormats: AbstractRendererFormats = StandardRendererFormats()
    override val rendererLayout: AbstractRendererLayout = StandardRendererLayout(rendererFormats)
    private val textFlatter: IndexedTextFlatter = IndexedTextFlatter(rendererLayout)

    override fun initialize(
        formatPath: Path,
        layoutPath: Path,
    ) {
        StandardRenderingParts.bootstrap()
        rendererFormats.initialize(formatPath) // formats 必须在 layout 之前初始化
        rendererLayout.initialize(layoutPath)
    }

    override fun render(item: PacketNekoStack, context: StandardContext?) {
        requireNotNull(context) { "context" }

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
                    is EmptyCore -> StandardRenderingParts.CELLULAR_EMPTY.process(collector, null)
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

        val lore = textFlatter.flatten(collector)
        val cmd = ItemModelDataLookup[item.id, item.variant]

        // 修改物品(原地)
        item.lore(lore)
        item.customModelData(cmd)
    }

    private inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }
}


//////


internal object StandardRenderingParts {
    @JvmField
    val ATTACK_SPEED: SimpleRenderingPart<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE: SimpleRenderingPart<ConstantCompositeAttribute, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_SKILL: SimpleRenderingPart<ConfiguredSkill, CellularSkillRendererFormat> = configure("cells/skills") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_EMPTY: SimpleRenderingPart<Nothing?, CellularEmptyRendererFormat> = configure("cells/empty") { _, format ->
        format.render()
    }

    @JvmField
    val CRATE: SimpleRenderingPart<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        val resolver = TagResolver.resolver(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里
        )
        format.render(resolver)
    }

    @JvmField
    val CUSTOM_NAME: SimpleRenderingPart<CustomName, SingleValueRendererFormat> = configure("custom_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val ELEMENTS: SimpleRenderingPart<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        format.render(data.elements, Element::displayName)
    }

    @JvmField
    val ENCHANTMENTS: SimpleRenderingPart<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val FIRE_RESISTANT: SimpleRenderingPart<FireResistant, SingleValueRendererFormat> = configure("fire_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: SimpleRenderingPart<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        val resolver = TagResolver.resolver(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
            Placeholder.component("eat_seconds", Component.text(data.eatSeconds)),
        )
        format.render(resolver)
    }

    @JvmField
    val ITEM_NAME: SimpleRenderingPart<ItemName, SingleValueRendererFormat> = configure("item_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val KIZAMIZ: SimpleRenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        format.render(data.kizamiz, Kizami::displayName)
    }

    @JvmField
    val LEVEL: SimpleRenderingPart<ItemLevel, SingleValueRendererFormat> = configure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: SimpleRenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        format.render(data.lore)
    }

    @JvmField
    val PORTABLE_CORE: SimpleRenderingPart<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: SimpleRenderingPart<ItemRarity, SingleValueRendererFormat> = configure("rarity") { data, format ->
        format.render(Placeholder.component("value", data.rarity.displayName))
    }

    fun bootstrap() = Unit // to explicitly initialize static block

    /**
     * @param id 用来定位配置文件中的节点
     * @param block 将数据渲染成文本的逻辑
     */
    private inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): SimpleRenderingPart<T, F> =
        SimpleRenderingPart(provideFormat<F>(id), block)

    private inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): SimpleRenderingPart2<T1, T2, F> =
        SimpleRenderingPart2(provideFormat<F>(id), block)

    private inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): SimpleRenderingPart3<T1, T2, T3, F> =
        SimpleRenderingPart3(provideFormat<F>(id), block)

    private inline fun <reified F : RendererFormat> provideFormat(id: String): Provider<F> {
        try {
            val added = StandardItemRenderer.rendererFormats.register(id, typeOf<F>())
            val format = StandardItemRenderer.rendererFormats.get0<F>(id)
            return format
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }
}


//////


//<editor-fold desc="RendererFormat">
@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: String = "Attack Speed: <value>",
) : RendererFormat.Simple {
    override val id = "attack_speed"
    override val index: Key = createIndex()

    override fun createTextMetaFactory(): TextMetaFactory {
        return SingleSimpleTextMetaFactory(namespace, id)
    }

    // TODO display2 mapping Enum to String
    fun render(data: ItemAttackSpeed): IndexedText {
        val resolver = Placeholder.component("value", Component.text(data.level.name))
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip, resolver)))
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: Ordinal,
) : RendererFormat.Dynamic<ConstantCompositeAttribute> {
    override fun computeIndex(data: ConstantCompositeAttribute): Key {
        val indexId = buildString {
            append(data.id)
            append('.')
            append(data.operation)
            data.element?.let {
                append('.')
                append(it.uniqueId)
            }
        }
        return Key.key(namespace, indexId)
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    }

    fun render(data: ConstantCompositeAttribute): IndexedText {
        val facade = AttributeRegistry.FACADES[data.id]
        val tooltip = facade.createTooltipLore(data)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    @ConfigSerializable
    data class Ordinal(
        @Setting @Required
        val element: List<String>,
        @Setting @Required
        val operation: List<String>,
    )
}

@ConfigSerializable
internal data class CellularSkillRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<ConfiguredSkill> {
    override fun computeIndex(data: ConfiguredSkill): Key {
        val dataId = data.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return SkillCoreTextMetaFactory(namespace)
    }

    fun render(data: ConfiguredSkill): IndexedText {
        val instance = data.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: List<Component> = listOf(Component.text("Empty Slot")),
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: Key = createIndex()

    override fun createTextMetaFactory(): TextMetaFactory {
        return EmptyCoreTextMetaFactory(namespace)
    }

    fun render(): IndexedText {
        return SimpleIndexedText(index, tooltip)
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")

    override fun computeIndex(data: PortableCore): Key {
        throw UnsupportedOperationException() // 直接在 render 函数中处理
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return PortableCoreTextMetaFactory(namespace)
    }

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.attribute.id)
        val facade = AttributeRegistry.FACADES[core.attribute.id]
        val tooltip = facade.createTooltipLore(core.attribute)
        return SimpleIndexedText(index, tooltip)
    }
}
//</editor-fold>


//////


//<editor-fold desc="TextMeta">
internal data class AttributeCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
    private val derived: Derived,
) : SimpleTextMeta {
    override fun generateIndexes(): List<DerivedIndex> {
        val sourceNamespace = sourceIndex.namespace()
        val sourceId = sourceIndex.value()
        val combiner = StringCombiner(sourceId, ".") {
            addList(derived.operationIndex)
            addList(derived.elementIndex, AttributeRegistry.FACADES[sourceId].components.hasComponent<CompositeAttributeComponent.Element>())
        }
        val combinations = combiner.combine()
        return combinations.map { Key.key(sourceNamespace, it) }
    }

    data class Derived(
        val operationIndex: List<String>,
        val elementIndex: List<String>,
    ) {
        init { // validate values
            this.operationIndex.forEach { Operation.byKeyOrThrow(it) }
            this.elementIndex.forEach { ElementRegistry.INSTANCES[it] }
        }
    }
}

internal data class AttributeCoreTextMetaFactory(
    override val namespace: String,
    private val operationIndex: List<String>,
    private val elementIndex: List<String>,
) : TextMetaFactory {
    override fun test(sourceIndex: Key): Boolean {
        return sourceIndex.namespace() == namespace && AttributeRegistry.FACADES.has(sourceIndex.value())
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        val derived = AttributeCoreTextMeta.Derived(operationIndex, elementIndex)
        return AttributeCoreTextMeta(sourceIndex, sourceOrdinal, defaultText, derived)
    }
}

internal data class SkillCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override fun generateIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }
}

internal data class SkillCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        val key = Key.key( // 技能的标识
            sourceIndex.value().substringBefore('/'),
            sourceIndex.value().substringAfter('/')
        )
        return sourceIndex.namespace() == namespace && SkillRegistry.INSTANCES.has(key)
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SkillCoreTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}

internal data class EmptyCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    // 根据 MAX_DISPLAY_COUNT 生成对应数量的 TooltipKey. 生成出来的格式为:
    // "namespace:value/0",
    // "namespace:value/1",
    // "namespace:value/2", ...
    override fun generateIndexes(): List<DerivedIndex> {
        val ret = mutableListOf<DerivedIndex>()
        for (i in 0 until MAX_DISPLAY_COUNT) {
            ret += derive(sourceIndex, i)
        }
        return ret
    }

    /**
     * 根据索引生成对应的 [DerivedIndex].
     */
    fun derive(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal): DerivedIndex {
        return Key.key("${sourceIndex.namespace()}:${sourceIndex.value()}/$sourceOrdinal")
    }

    companion object Shared {
        const val MAX_DISPLAY_COUNT = 10
    }
}

internal data class EmptyCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == SOURCE_INDEX_VALUE
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return EmptyCoreTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }

    companion object Shared {
        const val SOURCE_INDEX_VALUE = "cells/empty"
    }
}

internal data class PortableCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == SOURCE_INDEX_VALUE
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SingleSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }

    companion object Shared {
        const val SOURCE_INDEX_VALUE = "portable_core"
    }
}
//</editor-fold>
