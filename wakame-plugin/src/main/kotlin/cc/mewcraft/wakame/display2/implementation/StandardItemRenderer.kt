/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.composite.*
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.components.ItemArrow
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.util.StringCombiner
import cc.mewcraft.wakame.util.value
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class StandardRendererFormats : AbstractRendererFormats()

internal class StandardRendererLayout(formats: AbstractRendererFormats) : AbstractRendererLayout(formats)

internal data object StandardContext // 等之后需要的时候, 改成 class 即可

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>() {
    override val name = "standard"
    override val rendererFormats = StandardRendererFormats()
    override val rendererLayout = StandardRendererLayout(rendererFormats)
    private val textFlatter = IndexedTextFlatter(rendererLayout)

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

        val collector = ObjectArrayList<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ARROW) { data -> StandardRenderingParts.ARROW.process(collector, data) }

        val components = item.components
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
        // components.process(ItemComponentTypes.CUSTOM_NAME) { data -> StandardRenderingParts.CUSTOM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.ELEMENTS) { data -> StandardRenderingParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> StandardRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FIRE_RESISTANT) { data -> StandardRenderingParts.FIRE_RESISTANT.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> StandardRenderingParts.FOOD.process(collector, data) }
        // components.process(ItemComponentTypes.ITEM_NAME) { data -> StandardRenderingParts.ITEM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.KIZAMIZ) { data -> StandardRenderingParts.KIZAMIZ.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> StandardRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.LORE) { data -> StandardRenderingParts.LORE.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> StandardRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.RARITY) { data -> StandardRenderingParts.RARITY.process(collector, data) }

        val minecraftLore = textFlatter.flatten(collector)
        val minecraftCmd = ItemModelDataLookup[item.id, item.variant]

        // 更新描述
        item.lore(minecraftLore)

        // 更新模型
        item.customModelData(minecraftCmd)

        // 擦除萌芽NBT
        // item.erase()

        // 隐藏客户端渲染
        item.showAttributeModifiers(false)
        // item.showCanBreak(false)
        // item.showCanPlaceOn(false)
        // item.showDyedColor(false)
        item.showEnchantments(false)
        // item.showJukeboxPlayable(false)
        // item.showStoredEnchantments(false)
        // item.showTrim(false)
        // item.showUnbreakable(false)
    }

    private inline fun <T> ItemTemplateMap.process(type: ItemTemplateType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }

    private inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }
}


//////


internal object StandardRenderingParts : RenderingParts() {
    @JvmField
    val ARROW: RenderingPart<ItemArrow, ListValueRendererFormat> = configure("arrow") { data, format ->
        format.render(
            Placeholder.component("pierce_level", Component.text(data.pierceLevel)),
            Placeholder.component("fire_ticks", Component.text(data.fireTicks)),
            Placeholder.component("hit_fire_ticks", Component.text(data.hitFireTicks)),
            Placeholder.component("hit_frozen_ticks", Component.text(data.hitFrozenTicks)),
            Placeholder.component("glow_ticks", Component.text(data.glowTicks)),
        )
    }

    @JvmField
    val ATTACK_SPEED: RenderingPart<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data.level)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE: RenderingPart<ConstantCompositeAttribute, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_SKILL: RenderingPart<ConfiguredSkill, CellularSkillRendererFormat> = configure("cells/skills") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_EMPTY: RenderingPart<Nothing?, CellularEmptyRendererFormat> = configure("cells/empty") { _, format ->
        format.render()
    }

    @JvmField
    val CRATE: RenderingPart<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里)
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = configure("custom_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        format.render(data.elements, Element::displayName)
    }

    @JvmField
    val ENCHANTMENTS: RenderingPart<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val FIRE_RESISTANT: RenderingPart<FireResistant, SingleValueRendererFormat> = configure("fire_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: RenderingPart<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
            Placeholder.component("eat_seconds", Component.text(data.eatSeconds)),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = configure("item_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val KIZAMIZ: RenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        format.render(data.kizamiz, Kizami::displayName)
    }

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = configure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        format.render(data.lore)
    }

    @JvmField
    val PORTABLE_CORE: RenderingPart<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingPart<ItemRarity, SingleValueRendererFormat> = configure("rarity") { data, format ->
        format.render(Placeholder.component("value", data.rarity.displayName))
    }
}


//////


//<editor-fold desc="RendererFormat">
@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id = "attack_speed"
    override val index = createIndex()

    override fun createTextMetaFactory(): TextMetaFactory {
        return SingleSimpleTextMetaFactory(namespace, id)
    }

    fun render(data: AttackSpeedLevel): IndexedText {
        val resolver = Placeholder.component("value", tooltip.level.getOrDefault(data.ordinal, UNKNOWN_LEVEL))
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip.line, resolver)))
    }

    @ConfigSerializable
    data class Tooltip(
        @Setting
        val line: String = "Attack Speed: <value>",
        @Setting
        val level: Map<Int, Component> = mapOf(
            0 to Component.text("Very Slow"),
            1 to Component.text("Slow"),
            2 to Component.text("Normal"),
            3 to Component.text("Fast"),
            4 to Component.text("Very Fast"),
            // 等攻速可自定义的时候, 这部分也要跟着重构一下
        ),
    )

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
        private val UNKNOWN_LEVEL = Component.text("???")
    }
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: Ordinal,
) : RendererFormat.Dynamic<ConstantCompositeAttribute> {
    fun render(data: ConstantCompositeAttribute): IndexedText {
        val facade = AttributeRegistry.FACADES[data.id]
        val tooltip = facade.createTooltipLore(data)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    override fun computeIndex(data: ConstantCompositeAttribute): Key {
        val indexId = buildString {
            append(data.id)
            append('.')
            append(data.operation.key)
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
    fun render(data: ConfiguredSkill): IndexedText {
        val instance = data.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    override fun computeIndex(data: ConfiguredSkill): Key {
        val dataId = data.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return SkillCoreTextMetaFactory(namespace)
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
    override val id = "cells/empty"
    override val index = createIndex()

    // 由于索引相同的 IndexedText 经过 IndexedTextFlatter 的处理后会去重,
    // 这里使用一个简单的算法, 来产生带有序数的 IndexedText 使得索引不再重复.
    private val tooltipCycle = IndexedTextCycle(limit = EmptyCoreTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(index.value { v -> "$v/$i" }, tooltip)
    }

    fun render(): IndexedText {
        return tooltipCycle.next()
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return EmptyCoreTextMetaFactory(namespace)
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.attribute.id)
        val facade = AttributeRegistry.FACADES[core.attribute.id]
        val tooltip = facade.createTooltipLore(core.attribute)
        return SimpleIndexedText(index, tooltip)
    }

    override fun computeIndex(data: PortableCore): Key {
        throw UnsupportedOperationException() // 直接在 render(...) 函数中处理
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return PortableCoreTextMetaFactory(namespace)
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
        // val key = Key.key(
        //     sourceIndex.value().substringBefore('/'),
        //     sourceIndex.value().substringAfter('/')
        // )
        // FIXME 临时方案, 理想中的技能 key 应该如上面注释所示
        //  也就是说, 如果 sourceIndex 是 skill:buff/potion_drop,
        //  那么对应的技能的 key 应该是 buff:potion_drop (???)
        return sourceIndex.namespace() == namespace && SkillRegistry.INSTANCES.has(sourceIndex)
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
    // 根据 MAX_DISPLAY_COUNT 生成对应数量的 DerivedIndex. 格式为:
    // "namespace:value/0",
    // "namespace:value/1",
    // "namespace:value/2",
    // ...
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
