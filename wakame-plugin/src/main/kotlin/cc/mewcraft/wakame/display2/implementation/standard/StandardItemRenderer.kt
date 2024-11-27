/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.SimpleTextMeta
import cc.mewcraft.wakame.display2.SourceIndex
import cc.mewcraft.wakame.display2.SourceOrdinal
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.EnchantmentRendererFormat
import cc.mewcraft.wakame.display2.implementation.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.ListValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMeta
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.CyclicIndexRule
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemAttackSpeed
import cc.mewcraft.wakame.item.components.ItemCrate
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.ItemArrow
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.hasComponent
import cc.mewcraft.wakame.util.StringCombiner
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class StandardRendererFormats(renderer: StandardItemRenderer) : AbstractRendererFormats(renderer)

internal class StandardRendererLayout(renderer: StandardItemRenderer) : AbstractRendererLayout(renderer)

internal data object StandardContext // 等之后需要的时候, 改成 class 即可

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>() {
    override val name = "standard"
    override val formats = StandardRendererFormats(this)
    override val layout = StandardRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(
        formatPath: Path,
        layoutPath: Path,
    ) {
        StandardRenderingParts.bootstrap()
        formats.initialize(formatPath) // formats 必须在 layout 之前初始化
        layout.initialize(layoutPath)
    }

    override fun render(item: PacketNekoStack, context: StandardContext?) {
        requireNotNull(context) { "context" }

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ARROW) { data -> StandardRenderingParts.ARROW.process(collector, data) }

        // 对于最可能被频繁修改的 `custom_name`, `item_name`, `lore` 直接读取配置模板里的内容
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> StandardRenderingParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> StandardRenderingParts.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> StandardRenderingParts.LORE.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ATTACK_SPEED) { data -> StandardRenderingParts.ATTACK_SPEED.process(collector, data) }
        components.process(ItemComponentTypes.CELLS) { data -> for ((_, cell) in data) renderCore(collector, cell.getCore()) }
        components.process(ItemComponentTypes.CRATE) { data -> StandardRenderingParts.CRATE.process(collector, data) }
        components.process(ItemComponentTypes.ELEMENTS) { data -> StandardRenderingParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> StandardRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FIRE_RESISTANT) { data -> StandardRenderingParts.FIRE_RESISTANT.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> StandardRenderingParts.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.KIZAMIZ) { data -> StandardRenderingParts.KIZAMIZ.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> StandardRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> StandardRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.RARITY) { data -> StandardRenderingParts.RARITY.process(collector, data) }
        components.process(ItemComponentTypes.STORED_ENCHANTMENTS) { data -> StandardRenderingParts.ENCHANTMENTS.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.lore = run {
            // 尝试在物品原本的 lore 的第一行插入我们渲染的 lore.
            // 如果原本的 lore 为空, 则直接使用我们渲染的 lore.
            // 如果原本的 lore 不为空, 则在渲染的 lore 和原本的 lore 之间插入一个空行.

            val lore = item.lore
            if (lore.isNullOrEmpty()) {
                itemLore
            } else {
                itemLore + buildList {
                    add(Component.empty())
                    addAll(lore)
                }
            }
        }
        item.customModelData = itemCustomModelData
        item.showAttributeModifiers(false)
        item.showEnchantments(false)
        item.showStoredEnchantments(false)
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, core: Core) {
        when (core) {
            is AttributeCore -> StandardRenderingParts.CELLULAR_ATTRIBUTE.process(collector, core)
            is SkillCore -> StandardRenderingParts.CELLULAR_SKILL.process(collector, core)
            is EmptyCore -> StandardRenderingParts.CELLULAR_EMPTY.process(collector, core)
        }
    }
}


//////


internal object StandardRenderingParts : RenderingParts(StandardItemRenderer) {
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
    val CELLULAR_ATTRIBUTE: RenderingPart<AttributeCore, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_SKILL: RenderingPart<SkillCore, CellularSkillRendererFormat> = configure("cells/skills") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_EMPTY: RenderingPart<EmptyCore, CellularEmptyRendererFormat> = configure("cells/empty") { data, format ->
        format.render(data)
    }

    @JvmField
    val CRATE: RenderingPart<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里)
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = CommonRenderingParts.ELEMENTS(this)

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
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val KIZAMIZ: RenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        format.render(data.kizamiz, Kizami::displayName)
    }

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = CommonRenderingParts.LEVEL(this)

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingParts.LORE(this)

    @JvmField
    val PORTABLE_CORE: RenderingPart<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingPart<ItemRarity, SingleValueRendererFormat> = CommonRenderingParts.RARITY(this)
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
    override val textMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

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
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)

    fun render(data: AttributeCore): IndexedText {
        return SimpleIndexedText(computeIndex(data), data.description)
    }

    /**
     * 实现要求: 返回值必须是 [AttributeCoreTextMeta.derivedIndexes] 的子集.
     */
    override fun computeIndex(data: AttributeCore): Key {
        return data.computeIndex(namespace)
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
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory = SkillCoreTextMetaFactory(namespace)

    fun render(data: SkillCore): IndexedText {
        val instance = data.skill.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    override fun computeIndex(data: SkillCore): Key {
        val dataId = data.skill.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
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

    private val cyclicIndexRule = CyclicIndexRule.SLASH
    override val textMetaFactory = CyclicTextMetaFactory(namespace, id, cyclicIndexRule)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(cyclicIndexRule.make(index, i), tooltip)
    }

    fun render(data: EmptyCore): IndexedText {
        return tooltipCycle.next()
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Simple {
    override val id = "portable_core"
    override val index = createIndex()
    override val textMetaFactory = PortableCoreTextMetaFactory(namespace)

    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore
            ?: return SimpleIndexedText(unknownIndex, listOf())
        return SimpleIndexedText(index, core.description)
    }
}
//</editor-fold>


//////


//<editor-fold desc="TextMeta">
internal data class AttributeCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
    private val derivation: DerivationRule,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    /**
     * 实现要求: 返回的列表必须是 [CellularAttributeRendererFormat.computeIndex] 的超集.
     */
    override fun deriveIndexes(): List<DerivedIndex> {
        val sourceNamespace = sourceIndex.namespace()
        val sourceId = sourceIndex.value()
        val combiner = StringCombiner(sourceId, ".") {
            addList(derivation.operationIndex)
            addList(derivation.elementIndex, AttributeRegistry.FACADES[sourceId].components.hasComponent<CompositeAttributeComponent.Element>())
        }
        val combinations = combiner.combine()
        return combinations.map { Key.key(sourceNamespace, it) }
    }

    data class DerivationRule(
        val operationIndex: List<String>,
        val elementIndex: List<String>,
    ) {
        init { // validate values
            this.operationIndex.forEach { Operation.byName(it) ?: error("'$it' is not a valid operation, check your renderer config") }
            this.elementIndex.forEach { ElementRegistry.INSTANCES.getOrNull(it) ?: error("'$it' is not a valid element, check your renderer config") }
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
        val derivationRule = AttributeCoreTextMeta.DerivationRule(operationIndex, elementIndex)
        return AttributeCoreTextMeta(sourceIndex, sourceOrdinal, defaultText, derivationRule)
    }
}

internal data class SkillCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    override fun deriveIndexes(): List<DerivedIndex> {
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

internal data class PortableCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == "portable_core"
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SingleSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}
//</editor-fold>
