package cc.mewcraft.wakame.item.display.implementation.modding_table

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.SimpleIndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.getDataOrDefault
import cc.mewcraft.wakame.item.getMeta
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.removeItalic
import cc.mewcraft.wakame.util.item.fastLore
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
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

@Init(InitStage.POST_WORLD)
internal object ModdingTableItemRenderer : AbstractItemRenderer<ItemStack, ModdingTableContext>() {
    override val name: String = "modding_table"
    override val formats = ModdingTableRendererFormatRegistry(this)
    override val layout = ModdingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        ModdingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: ModdingTableContext?) {
        requireNotNull(context) { "context" }

        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // ItemMetaTypes
        ModdingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME))
        ModdingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))

        // ItemDataTypes
        ModdingTableRenderingHandlerRegistry.ELEMENT.process(collector, item.getData(ItemDataTypes.ELEMENT))
        ModdingTableRenderingHandlerRegistry.LEVEL.process(collector, item.getData(ItemDataTypes.LEVEL))
        ModdingTableRenderingHandlerRegistry.RARITY.process(collector, item.getData(ItemDataTypes.RARITY), item.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO))

        if (context is ModdingTableContext.Input) {
            val coreContainer = item.getData(ItemDataTypes.CORE_CONTAINER)
            if (coreContainer != null) {
                for ((id, core) in coreContainer) {
                    when (core) {
                        is AttributeCore -> ModdingTableRenderingHandlerRegistry.CORE_ATTRIBUTE_MAIN_IN.process(collector, id, core, context)
                        is EmptyCore -> ModdingTableRenderingHandlerRegistry.CORE_EMPTY_IN.process(collector, id, context)
                        is VirtualCore -> IndexedText.NO_OP
                    }
                }
            }
        }

        if (context is ModdingTableContext.Output) {
            val coreContainer = item.getData(ItemDataTypes.CORE_CONTAINER)
            if (coreContainer != null) {
                for ((id, core) in coreContainer) {
                    when (core) {
                        is AttributeCore -> ModdingTableRenderingHandlerRegistry.CORE_ATTRIBUTE_MAIN_OUT.process(collector, id, core, context)
                        is EmptyCore -> ModdingTableRenderingHandlerRegistry.CORE_EMPTY_OUT.process(collector, id, context)
                        is VirtualCore -> IndexedText.NO_OP
                    }
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

        val koishLore = textAssembler.assemble(collector)

        // 将修改应用到物品上
        item.fastLore(koishLore)
    }
}


//////


internal object ModdingTableRenderingHandlerRegistry : RenderingHandlerRegistry(ModdingTableItemRenderer) {

    @JvmField
    val CORE_ATTRIBUTE_MAIN_IN: RenderingHandler3<String, AttributeCore, ModdingTableContext, CoreAttributeRendererFormat> = configure3("core/attributes/in") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CORE_ATTRIBUTE_MAIN_OUT: RenderingHandler3<String, AttributeCore, ModdingTableContext, CoreAttributeRendererFormat> = configure3("core/attributes/out") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CORE_EMPTY_IN: RenderingHandler2<String, ModdingTableContext, CoreEmptyRendererFormat> = configure2("core/empty/in") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val CORE_EMPTY_OUT: RenderingHandler2<String, ModdingTableContext, CoreEmptyRendererFormat> = configure2("core/empty/out") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val REPLACE_IN: RenderingHandler2<Core, ModdingTableContext, HardcodedRendererFormat> = configure2("replace_input") { core, context, format ->
        SimpleIndexedText(format.index, core.description)
    }

    @JvmField
    val REFORGE_COST: RenderingHandler<ModdingTableContext, HardcodedRendererFormat> = configure("reforge_cost") { context, format ->
        SimpleIndexedText(format.index, context.session.latestResult.reforgeCost.description.removeItalic)
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
