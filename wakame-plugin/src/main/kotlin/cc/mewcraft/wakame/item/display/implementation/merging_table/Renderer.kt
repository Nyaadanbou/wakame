package cc.mewcraft.wakame.item.display.implementation.merging_table

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.ItemLevel
import cc.mewcraft.wakame.item.data.impl.ReforgeHistory
import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
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
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.item.fastLore
import io.papermc.paper.datacomponent.DataComponentTypes
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class MergingTableRendererFormatRegistry(renderer: MergingTableItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class MergingTableRendererLayout(renderer: MergingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface MergingTableContext {
    data class MergeInputSlot(val session: MergingSession) : MergingTableContext
    data class MergeOutputSlot(val session: MergingSession) : MergingTableContext
}

@Init(InitStage.POST_WORLD)
internal object MergingTableItemRenderer : AbstractItemRenderer<MergingTableContext>() {
    override val name: String = "merging_table"
    override val formats = MergingTableRendererFormatRegistry(this)
    override val layout = MergingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        MergingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: MergingTableContext?) {
        requireNotNull(context) { "context" }

        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // ItemMetaTypes
        MergingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))
        MergingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME))

        // ItemDataTypes
        MergingTableRenderingHandlerRegistry.ELEMENT.process(collector, item.getData(ItemDataTypes.ELEMENT))
        MergingTableRenderingHandlerRegistry.LEVEL.process(collector, item.getData(ItemDataTypes.LEVEL))
        MergingTableRenderingHandlerRegistry.RARITY.process(collector, item.getData(ItemDataTypes.RARITY), item.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO))
        when (context) {
            is MergingTableContext.MergeInputSlot -> MergingTableRenderingHandlerRegistry.MERGE_IN.process(collector, item.getData(ItemDataTypes.CORE), context)
            is MergingTableContext.MergeOutputSlot -> MergingTableRenderingHandlerRegistry.MERGE_OUT.process(collector, item.getData(ItemDataTypes.CORE), context)
        }

        val koishLore = textAssembler.assemble(collector)

        // 应用修改到物品上
        item.fastLore(koishLore)

        // 本渲染器专门处理放在菜单里面的物品,
        // 而这些物品有些时候会被玩家(用铁砧)修改 `minecraft:custom_name`
        // 导致在菜单里显示的是玩家自己设置的(奇葩)名字.
        // 我们在这里统一清除掉这个组件.
        item.resetData(DataComponentTypes.CUSTOM_NAME)
    }
}

internal object MergingTableRenderingHandlerRegistry : RenderingHandlerRegistry(MergingTableItemRenderer) {

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENT: RenderingHandler<Set<RegistryEntry<Element>>, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENT(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    // 渲染放在输入容器的便携核心
    @JvmField
    val MERGE_IN: RenderingHandler2<Core, MergingTableContext.MergeInputSlot, CoreRendererFormat> = configure2("merge_input") { data, context, format ->
        format.render(data)
    }

    // 渲染放在输出容器的便携核心
    @JvmField
    val MERGE_OUT: RenderingHandler2<Core, MergingTableContext.MergeOutputSlot, MergeOutputRendererFormat> = configure2("merge_output") { data, context, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingHandler2<RegistryEntry<Rarity>, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)
}
