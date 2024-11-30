/**
 * 有关*重造台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.rerolling_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart3
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.CyclicIndexRule
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.DifferenceFormat
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.StandaloneCellRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMeta
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.standard.SkillCoreTextMetaFactory
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class RerollingTableRendererFormats(renderer: RerollingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class RerollingTableRendererLayout(renderer: RerollingTableItemRenderer) : AbstractRendererLayout(renderer)

internal data class RerollingTableContext(
    val session: RerollingSession,
    val slot: Slot = Slot.UNDEFINED,
) {
    enum class Slot {
        INPUT, OUTPUT, UNDEFINED
    }
}

internal object RerollingTableItemRenderer : AbstractItemRenderer<NekoStack, RerollingTableContext>() {
    override val name: String = "rerolling_table"
    override val formats = RerollingTableRendererFormats(this)
    override val layout = RerollingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RerollingTableRenderingParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: RerollingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> RerollingTableRenderingParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> RerollingTableRenderingParts.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.CELLS) { data -> for ((id, cell) in data) renderCore(collector, id, cell, context) }
        components.process(ItemComponentTypes.STANDALONE_CELL) { data -> RerollingTableRenderingParts.STANDALONE_CELL.process(collector, item, data, context) }
        components.process(ItemComponentTypes.LEVEL) { data -> RerollingTableRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY) { data -> RerollingTableRenderingParts.RARITY.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase() // 这是呈现给玩家的最后一环, 可以 erase

        item.unsafeEdit {
            lore = itemLore
            customModelData = itemCustomModelData
            showNothing()
        }
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, id: String, cell: Cell, context: RerollingTableContext) {
        val core = cell.getCore()
        val slot = context.slot
        when (slot) {
            RerollingTableContext.Slot.INPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingParts.CELLULAR_ATTRIBUTE_IN.process(collector, id, core, context)
                    is SkillCore -> RerollingTableRenderingParts.CELLULAR_SKILL_IN.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingParts.CELLULAR_EMPTY_IN.process(collector, id, core, context)
                }
            }

            RerollingTableContext.Slot.OUTPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingParts.CELLULAR_ATTRIBUTE_OUT.process(collector, id, core, context)
                    is SkillCore -> RerollingTableRenderingParts.CELLULAR_SKILL_OUT.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingParts.CELLULAR_EMPTY_OUT.process(collector, id, core, context)
                }
            }

            RerollingTableContext.Slot.UNDEFINED -> {}
        }
    }
}


//////


internal object RerollingTableRenderingParts : RenderingParts(RerollingTableItemRenderer) {
    @JvmField
    val CELLULAR_ATTRIBUTE_IN: RenderingPart3<String, AttributeCore, RerollingTableContext, CellularAttributeRendererFormat> =
        configure3("cells/attributes/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_ATTRIBUTE_OUT: RenderingPart3<String, AttributeCore, RerollingTableContext, CellularAttributeRendererFormat> =
        configure3("cells/attributes/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_SKILL_IN: RenderingPart3<String, SkillCore, RerollingTableContext, CellularSkillRendererFormat> =
        configure3("cells/skills/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_SKILL_OUT: RenderingPart3<String, SkillCore, RerollingTableContext, CellularSkillRendererFormat> =
        configure3("cells/skills/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_EMPTY_IN: RenderingPart3<String, EmptyCore, RerollingTableContext, CellularEmptyRendererFormat> =
        configure3("cells/empty/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_EMPTY_OUT: RenderingPart3<String, EmptyCore, RerollingTableContext, CellularEmptyRendererFormat> =
        configure3("cells/empty/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = CommonRenderingParts.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = CommonRenderingParts.LEVEL(this)

    @JvmField
    val RARITY: RenderingPart<ItemRarity, SingleValueRendererFormat> = CommonRenderingParts.RARITY(this)

    @JvmField
    val STANDALONE_CELL: RenderingPart3<NekoStack, StandaloneCell, RerollingTableContext, StandaloneCellRendererFormat> = configure3("standalone_cell") { item, cell, context, format ->
        val coreText = cell.core.description
        val modCount = item.reforgeHistory.modCount
        val modLimit = context.session.itemRule?.modLimit ?: 0
        format.render(coreText, modCount, modLimit)
    }

    // TODO 让渲染器负责渲染重造的花费
    // val REFORGE_COST
}


//////


// 开发日记 2024/10/25
// 对于如何渲染要重造的核孔这个问题, 直接把*原输入*在输出容器里显示,
// 但把要重造的核孔划上删除线并加上类似“???”的前缀/后缀,
// 这样应该就足矣表示这个核孔将要经历重造了.

@ConfigSerializable
internal data class RerollingDifferenceFormats(
    @Setting
    val changeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val unchangeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val selected: DifferenceFormat = DifferenceFormat(),
    @Setting
    val unselected: DifferenceFormat = DifferenceFormat(),
) {
    /**
     * @param id 核孔的 id
     * @param source 原核心的描述
     * @param context 重造台的上下文
     * @return 基于 [id], [core], [context] 生成的 [IndexedText]
     */
    fun render(id: String, source: List<Component>, context: RerollingTableContext): List<Component> {
        val selectionMap = context.session.selectionMap
        val selection = selectionMap[id]

        var result = source

        if (selection.changeable) {
            result = changeable.process(result)

            if (selection.selected) {
                result = selected.process(result)
            } else {
                result = unselected.process(result)
            }

        } else {
            result = unchangeable.process(result)
        }

        return result
    }
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: Ordinal,
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)

    /**
     * @param id 核孔的 id
     * @param core 属性核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: AttributeCore, context: RerollingTableContext): IndexedText {
        val original = core.description
        val processed = differenceFormats.render(id, original, context)
        return SimpleIndexedText(computeIndex(core), processed)
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
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory = SkillCoreTextMetaFactory(namespace)

    /**
     * @param id 核孔的 id
     * @param core 技能核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: SkillCore, context: RerollingTableContext): IndexedText {
        val original = core.description
        val processed = differenceFormats.render(id, original, context)
        return SimpleIndexedText(computeIndex(core), processed)
    }

    override fun computeIndex(data: SkillCore): Key {
        val skill = data.skill
        val dataId = skill.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: List<Component> = listOf(text("Empty Slot")),
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Simple {
    override val id = "cells/empty"
    override val index = createIndex()

    private val cyclicIndexRule = CyclicIndexRule.SLASH
    override val textMetaFactory = CyclicTextMetaFactory(namespace, id, cyclicIndexRule)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(cyclicIndexRule.make(index, i), tooltip)
    }

    /**
     * @param id 核孔的 id
     * @param core 空核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: EmptyCore, context: RerollingTableContext): IndexedText {
        val next = tooltipCycle.next()
        val original = next.text
        val processed = differenceFormats.render(id, original, context)
        return next.copy(text = processed)
    }
}


//////

