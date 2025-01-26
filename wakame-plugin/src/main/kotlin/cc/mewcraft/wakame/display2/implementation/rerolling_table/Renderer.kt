package cc.mewcraft.wakame.display2.implementation.rerolling_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
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

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object RerollingTableItemRenderer : AbstractItemRenderer<NekoStack, RerollingTableContext>() {
    override val name: String = "rerolling_table"
    override val formats = RerollingTableRendererFormatRegistry(this)
    override val layout = RerollingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    fun reload() {
        loadDataFromConfigs()
    }

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
        components.process(ItemComponentTypes.LEVEL) { data -> RerollingTableRenderingHandlerRegistry.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1: ItemRarity = data1 ?: return@process
            val data2: ReforgeHistory = data2 ?: ReforgeHistory.ZERO
            RerollingTableRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }

        val itemLore = textAssembler.assemble(collector)

        item.erase() // 这是呈现给玩家的最后一环, 可以 erase

        item.unsafeEdit {
            lore = itemLore
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
                    is AbilityCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ABILITY_IN.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_EMPTY_IN.process(collector, id, core, context)
                }
            }

            RerollingTableContext.Slot.OUTPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_OUT.process(collector, id, core, context)
                    is AbilityCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ABILITY_OUT.process(collector, id, core, context)
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
    val CELLULAR_ABILITY_IN: RenderingHandler3<String, AbilityCore, RerollingTableContext, CellularAbilityRendererFormat> =
        configure3("cells/abilities/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CELLULAR_ABILITY_OUT: RenderingHandler3<String, AbilityCore, RerollingTableContext, CellularAbilityRendererFormat> =
        configure3("cells/abilities/out") { id, core, context, format ->
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
}
