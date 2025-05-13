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
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.AttributeCore
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.item2.data.impl.EmptyCore
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.item2.data.impl.ReforgeHistory
import cc.mewcraft.wakame.item2.data.impl.VirtualCore
import cc.mewcraft.wakame.item2.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.hideAll
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
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
internal object RerollingTableItemRenderer : AbstractItemRenderer<RerollingTableContext>() {
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

    override fun render(item: ItemStack, context: RerollingTableContext?) {
        requireNotNull(context) { "context" }

        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        item.process(ItemMetaTypes.CUSTOM_NAME) { data -> RerollingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.ITEM_NAME) { data -> RerollingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }

        item.process(ItemDataTypes.CORE_CONTAINER) { data -> for ((id, core) in data) renderCore(collector, id, core, context) }
        item.process(ItemDataTypes.LEVEL) { data -> RerollingTableRenderingHandlerRegistry.LEVEL.process(collector, data) }
        item.process(ItemDataTypes.RARITY, ItemDataTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            RerollingTableRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }

        val lore = textAssembler.assemble(collector)
        item.fastLore(lore)

        item.hideAll()
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, id: String, core: Core, context: RerollingTableContext) {
        val slot = context.slot
        when (slot) {
            RerollingTableContext.Slot.INPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_IN.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_EMPTY_IN.process(collector, id, core, context)
                    is VirtualCore -> IndexedText.NOP
                }
            }

            RerollingTableContext.Slot.OUTPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_OUT.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CELLULAR_EMPTY_OUT.process(collector, id, core, context)
                    is VirtualCore -> IndexedText.NOP
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
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingHandler<Set<RegistryEntry<Element>>, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    @JvmField
    val RARITY: RenderingHandler2<RegistryEntry<Rarity>, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)
}
