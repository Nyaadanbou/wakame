package cc.mewcraft.wakame.display2.implementation.rerolling_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormatRegistry
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.RenderingHandler
import cc.mewcraft.wakame.display2.implementation.RenderingHandler2
import cc.mewcraft.wakame.display2.implementation.RenderingHandler3
import cc.mewcraft.wakame.display2.implementation.RenderingHandlerRegistry
import cc.mewcraft.wakame.display2.implementation.common.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
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

internal class RerollingTableRendererFormatRegistry(renderer: RerollingTableItemRenderer) : AbstractRendererFormatRegistry(renderer)

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
    override val formats = RerollingTableRendererFormatRegistry(this)
    override val layout = RerollingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RerollingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: RerollingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> RerollingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> RerollingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.CELLS) { data -> for ((id, cell) in data) renderCore(collector, id, cell, context) }
        components.process(ItemComponentTypes.STANDALONE_CELL) { data -> RerollingTableRenderingHandlerRegistry.STANDALONE_CELL.process(collector, item, data, context) }
        components.process(ItemComponentTypes.LEVEL) { data -> RerollingTableRenderingHandlerRegistry.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1: ItemRarity = data1 ?: return@process
            val data2: ReforgeHistory = data2 ?: ReforgeHistory.ZERO
            RerollingTableRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
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
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_IN.process(collector, id, core, context)
                    is SkillCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_SKILL_IN.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_EMPTY_IN.process(collector, id, core, context)
                }
            }

            RerollingTableContext.Slot.OUTPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_OUT.process(collector, id, core, context)
                    is SkillCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_SKILL_OUT.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_EMPTY_OUT.process(collector, id, core, context)
                }
            }

            RerollingTableContext.Slot.UNDEFINED -> {}
        }
    }
}

internal object RerollingTableRenderingHandlerRegistry : RenderingHandlerRegistry(RerollingTableItemRenderer) {
    @JvmField
    val CELLULAR_ATTRIBUTE_IN: RenderingHandler3<String, AttributeCore, RerollingTableContext, CellularAttributeRendererFormat> =
        configure3("cells/attributes/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_ATTRIBUTE_OUT: RenderingHandler3<String, AttributeCore, RerollingTableContext, CellularAttributeRendererFormat> =
        configure3("cells/attributes/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_SKILL_IN: RenderingHandler3<String, SkillCore, RerollingTableContext, CellularSkillRendererFormat> =
        configure3("cells/skills/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_SKILL_OUT: RenderingHandler3<String, SkillCore, RerollingTableContext, CellularSkillRendererFormat> =
        configure3("cells/skills/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_EMPTY_IN: RenderingHandler3<String, EmptyCore, RerollingTableContext, CellularEmptyRendererFormat> =
        configure3("cells/empty/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_EMPTY_OUT: RenderingHandler3<String, EmptyCore, RerollingTableContext, CellularEmptyRendererFormat> =
        configure3("cells/empty/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<CustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingHandler<ItemElements, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<ItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    @JvmField
    val RARITY: RenderingHandler2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)

    @JvmField
    val STANDALONE_CELL: RenderingHandler3<NekoStack, StandaloneCell, RerollingTableContext, StandaloneCellRendererFormat> = configure3("standalone_cell") { item, cell, context, format ->
        val coreText = cell.core.description
        val modCount = item.reforgeHistory.modCount
        val modLimit = context.session.itemRule?.modLimit ?: 0
        format.render(coreText, modCount, modLimit)
    }

    // TODO 让渲染器负责渲染重造的花费
    // val REFORGE_COST
}
