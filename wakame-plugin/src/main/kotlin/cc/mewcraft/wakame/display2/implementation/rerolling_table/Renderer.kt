package cc.mewcraft.wakame.display2.implementation.rerolling_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart2
import cc.mewcraft.wakame.display2.implementation.RenderingPart3
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.StandaloneCellRendererFormat
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ReforgeHistory
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
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1: ItemRarity = data1 ?: return@process
            val data2: ReforgeHistory = data2 ?: ReforgeHistory.ZERO
            RerollingTableRenderingParts.RARITY.process(collector, data1, data2)
        }

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
    val RARITY: RenderingPart2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingParts.RARITY(this)

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
