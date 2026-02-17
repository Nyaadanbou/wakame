package cc.mewcraft.wakame.item.display.implementation.rerolling_table

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.AggregateValueRendererFormat
import cc.mewcraft.wakame.item.display.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.item.display.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.item.display.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.getDataOrDefault
import cc.mewcraft.wakame.item.getMeta
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.fastLore
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

@Init(InitStage.POST_WORLD)
internal object RerollingTableItemRenderer : AbstractItemRenderer<ItemStack, RerollingTableContext>() {
    override val name: String = "rerolling_table"
    override val formats = RerollingTableRendererFormatRegistry(this)
    override val layout = RerollingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

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

        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // ItemMetaTypes
        RerollingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))
        RerollingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME))

        // ItemDataTypes
        val coreContainer = item.getData(ItemDataTypes.CORE_CONTAINER)
        if (coreContainer != null) {
            for ((id, core) in coreContainer) {
                renderCore(collector, id, core, context)
            }
        }
        RerollingTableRenderingHandlerRegistry.ELEMENT.process(collector, item.getData(ItemDataTypes.ELEMENT))
        RerollingTableRenderingHandlerRegistry.LEVEL.process(collector, item.getData(ItemDataTypes.LEVEL))
        RerollingTableRenderingHandlerRegistry.RARITY.process(collector, item.getData(ItemDataTypes.RARITY), item.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO))

        val koishLore = textAssembler.assemble(collector)

        // 应用修改到物品上
        item.fastLore(koishLore)
    }

    private fun renderCore(collector: ReferenceOpenHashSet<IndexedText>, id: String, core: Core, context: RerollingTableContext) {
        val slot = context.slot
        when (slot) {
            RerollingTableContext.Slot.INPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CORE_ATTRIBUTE_IN.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CORE_EMPTY_IN.process(collector, id, core, context)
                    is VirtualCore -> IndexedText.NOP
                }
            }

            RerollingTableContext.Slot.OUTPUT -> {
                when (core) {
                    is AttributeCore -> RerollingTableRenderingHandlerRegistry.CORE_ATTRIBUTE_OUT.process(collector, id, core, context)
                    is EmptyCore -> RerollingTableRenderingHandlerRegistry.CORE_EMPTY_OUT.process(collector, id, core, context)
                    is VirtualCore -> IndexedText.NOP
                }
            }

            RerollingTableContext.Slot.UNDEFINED -> {}
        }
    }
}

internal object RerollingTableRenderingHandlerRegistry : RenderingHandlerRegistry(RerollingTableItemRenderer) {

    @JvmField
    val CORE_ATTRIBUTE_IN: RenderingHandler3<String, AttributeCore, RerollingTableContext, CoreAttributeRendererFormat> =
        configure3("core/attributes/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CORE_ATTRIBUTE_OUT: RenderingHandler3<String, AttributeCore, RerollingTableContext, CoreAttributeRendererFormat> =
        configure3("core/attributes/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CORE_EMPTY_IN: RenderingHandler3<String, EmptyCore, RerollingTableContext, CoreEmptyRendererFormat> =
        configure3("core/empty/in") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CORE_EMPTY_OUT: RenderingHandler3<String, EmptyCore, RerollingTableContext, CoreEmptyRendererFormat> =
        configure3("core/empty/out") { id, core, context, format ->
            format.render(id, core, context)
        }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENT: RenderingHandler<Set<RegistryEntry<Element>>, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENT(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    @JvmField
    val RARITY: RenderingHandler2<RegistryEntry<Rarity>, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)
}
