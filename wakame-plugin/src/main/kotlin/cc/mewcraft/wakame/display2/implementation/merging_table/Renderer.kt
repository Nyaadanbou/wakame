package cc.mewcraft.wakame.display2.implementation.merging_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import java.nio.file.Path


internal class MergingTableRendererFormatRegistry(renderer: MergingTableItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class MergingTableRendererLayout(renderer: MergingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface MergingTableContext {
    data class MergeInputSlot(val session: MergingSession) : MergingTableContext
    data class MergeOutputSlot(val session: MergingSession) : MergingTableContext
}

@Init(
    stage = InitStage.POST_WORLD
)
@Reload()
internal object MergingTableItemRenderer : AbstractItemRenderer<NekoStack, MergingTableContext>() {
    override val name: String = "merging_table"
    override val formats = MergingTableRendererFormatRegistry(this)
    override val layout = MergingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        MergingTableRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: MergingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> MergingTableRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> MergingTableRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ELEMENTS) { data -> MergingTableRenderingHandlerRegistry.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> MergingTableRenderingHandlerRegistry.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            MergingTableRenderingHandlerRegistry.RARITY.process(collector, data1, data2)
        }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data ->
            when (context) {
                is MergingTableContext.MergeInputSlot -> MergingTableRenderingHandlerRegistry.MERGE_IN.process(collector, data, context)
                is MergingTableContext.MergeOutputSlot -> MergingTableRenderingHandlerRegistry.MERGE_OUT.process(collector, data, context)
            }
        }

        val itemLore = textAssembler.assemble(collector)

        item.erase()

        item.unsafeEdit {
            // 本 ItemRenderer 专门渲染放在菜单里面的物品,
            // 而这些物品有些时候会被玩家(用铁砧)修改 `minecraft:custom_name`
            // 导致在菜单里显示的是玩家自己设置的(奇葩)名字.
            // 我们在这里统一清除掉这个组件.
            customName = null

            lore = itemLore
            showNothing()
        }
    }

    @InitFun
    fun onPostWorld() {
        initialize0()
    }

    @ReloadFun
    fun onReload() {
        initialize0()
    }
}

internal object MergingTableRenderingHandlerRegistry : RenderingHandlerRegistry(MergingTableItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingHandler<CustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingHandler<ItemElements, AggregateValueRendererFormat> = CommonRenderingHandlers.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<ItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingHandler<ItemLevel, SingleValueRendererFormat> = CommonRenderingHandlers.LEVEL(this)

    // 渲染放在输入容器的便携核心
    @JvmField
    val MERGE_IN: RenderingHandler2<PortableCore, MergingTableContext.MergeInputSlot, PortableCoreRendererFormat> = configure2("merge_input") { data, context, format ->
        format.render(data)
    }

    // 渲染放在输出容器的便携核心
    @JvmField
    val MERGE_OUT: RenderingHandler2<PortableCore, MergingTableContext.MergeOutputSlot, MergeOutputRendererFormat> = configure2("merge_output") { data, context, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: RenderingHandler2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingHandlers.RARITY(this)
}
