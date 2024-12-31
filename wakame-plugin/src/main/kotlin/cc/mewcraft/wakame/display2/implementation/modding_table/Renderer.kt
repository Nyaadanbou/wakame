package cc.mewcraft.wakame.display2.implementation.modding_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.SimpleIndexedText
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
import cc.mewcraft.wakame.display2.implementation.common.HardcodedRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.removeItalic
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import java.nio.file.Path


internal class ModdingTableRendererFormatRegistry(renderer: ModdingTableItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class ModdingTableRendererLayout(renderer: ModdingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface ModdingTableContext {
    val session: ModdingSession

    /**
     * 用于输入的主要物品, 也就是需要被定制的物品.
     */
    data class Input(override val session: ModdingSession) : ModdingTableContext

    /**
     * 用于输出的主要物品, 也就是经过定制后的物品.
     */
    data class Output(override val session: ModdingSession) : ModdingTableContext

    /**
     * 用于便携式核心.
     */
    data class Replace(override val session: ModdingSession, val replace: ModdingSession.Replace) : ModdingTableContext
}

internal object ModdingTableItemRenderer : AbstractItemRenderer<NekoStack, ModdingTableContext>() {
    override val name: String = "modding_table"
    override val formats = ModdingTableRendererFormatRegistry(this)
    override val layout = ModdingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        ModdingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: ModdingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> ModdingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> ModdingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ELEMENTS) { data -> ModdingTableRenderingHandlerRegistry.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> ModdingTableRenderingHandlerRegistry.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            ModdingTableRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }

        if (context is ModdingTableContext.Input) {
            components.process(ItemComponentTypes.CELLS) { data ->
                for ((_, cell) in data) when (val core = cell.getCore()) {
                    is AttributeCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_MAIN_IN.process(collector, cell.getId(), core, context)
                    is AbilityCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_ABILITY_IN.process(collector, cell.getId(), core, context)
                    is EmptyCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_EMPTY_IN.process(collector, cell.getId(), context)
                }
            }
        }

        if (context is ModdingTableContext.Output) {
            components.process(ItemComponentTypes.CELLS) { data ->
                for ((_, cell) in data) when (val core = cell.getCore()) {
                    is AttributeCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_ATTRIBUTE_MAIN_OUT.process(collector, cell.getId(), core, context)
                    is AbilityCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_ABILITY_OUT.process(collector, cell.getId(), core, context)
                    is EmptyCore -> ModdingTableRenderingHandlerRegistry.CELLULAR_EMPTY_OUT.process(collector, cell.getId(), context)
                }
            }

            // 输出物品需要渲染定制花费
            ModdingTableRenderingHandlerRegistry.REFORGE_COST.process(collector, context)
        }

        if (context is ModdingTableContext.Replace) {
            val augment = context.replace.augment
            if (augment != null) {
                ModdingTableRenderingHandlerRegistry.REPLACE_IN.process(collector, augment, context)
            }
        }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.unsafeEdit {
            lore = itemLore
            customModelData = itemCustomModelData
            showNothing()
        }
    }
}


//////


internal object ModdingTableRenderingHandlerRegistry : RenderingHandlerRegistry(ModdingTableItemRenderer) {

    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_IN: RenderingHandler3<String, AttributeCore, ModdingTableContext, CellularAttributeRendererFormat> = configure3("cells/attributes/in") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_OUT: RenderingHandler3<String, AttributeCore, ModdingTableContext, CellularAttributeRendererFormat> = configure3("cells/attributes/out") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CELLULAR_ABILITY_IN: RenderingHandler3<String, AbilityCore, ModdingTableContext, CellularAbilityRendererFormat> = configure3("cells/abilities/in") { id, ability, context, format ->
        format.render(id, ability, context)
    }

    @JvmField
    val CELLULAR_ABILITY_OUT: RenderingHandler3<String, AbilityCore, ModdingTableContext, CellularAbilityRendererFormat> = configure3("cells/abilities/out") { id, ability, context, format ->
        format.render(id, ability, context)
    }

    @JvmField
    val CELLULAR_EMPTY_IN: RenderingHandler2<String, ModdingTableContext, CellularEmptyRendererFormat> = configure2("cells/empty/in") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val CELLULAR_EMPTY_OUT: RenderingHandler2<String, ModdingTableContext, CellularEmptyRendererFormat> = configure2("cells/empty/out") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val REPLACE_IN: RenderingHandler2<PortableCore, ModdingTableContext, HardcodedRendererFormat> = configure2("replace_input") { core, context, format ->
        SimpleIndexedText(format.index, core.description)
    }

    @JvmField
    val REFORGE_COST: RenderingHandler<ModdingTableContext, HardcodedRendererFormat> = configure("reforge_cost") { context, format ->
        SimpleIndexedText(format.index, context.session.latestResult.reforgeCost.description.removeItalic)
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
