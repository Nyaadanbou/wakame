package cc.mewcraft.wakame.item.display.implementation.simple

import cc.mewcraft.wakame.item.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item.display.IndexedText
import cc.mewcraft.wakame.item.display.TextAssembler
import cc.mewcraft.wakame.item.display.implementation.*
import cc.mewcraft.wakame.item.display.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.item.display.implementation.common.ExtraLoreRendererFormat
import cc.mewcraft.wakame.item.display.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.item.getMeta
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.isNetworkRewrite
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ExtraLore
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.fastLore
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class SimpleRendererFormatRegistry : AbstractRendererFormatRegistry(SimpleItemRenderer)

internal class SimpleItemRendererLayout : AbstractRendererLayout(SimpleItemRenderer)

@Init(InitStage.POST_WORLD)
internal object SimpleItemRenderer : AbstractItemRenderer<ItemStack, Nothing>() {
    override val name: String = "simple"
    override val formats: AbstractRendererFormatRegistry = SimpleRendererFormatRegistry()
    override val layout: AbstractRendererLayout = SimpleItemRendererLayout()
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        SimpleRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: Nothing?) {
        // 这里的物品不应该被网络重写
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // ItemMetaTypes
        SimpleRenderingHandlerRegistry.CUSTOM_NAME.process(collector, item.getMeta(ItemMetaTypes.CUSTOM_NAME))
        SimpleRenderingHandlerRegistry.ITEM_NAME.process(collector, item.getMeta(ItemMetaTypes.ITEM_NAME))
        SimpleRenderingHandlerRegistry.LORE.process(collector, item.getProp(ItemPropTypes.EXTRA_LORE))

        val koishLore = textAssembler.assemble(collector)

        // 应用修改到物品上
        item.fastLore(koishLore)
    }
}

internal object SimpleRenderingHandlerRegistry : RenderingHandlerRegistry(SimpleItemRenderer) {

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)
}

