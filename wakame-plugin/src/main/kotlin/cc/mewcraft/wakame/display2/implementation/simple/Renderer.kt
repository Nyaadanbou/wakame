package cc.mewcraft.wakame.display2.implementation.simple

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormatRegistry
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.RenderingHandler
import cc.mewcraft.wakame.display2.implementation.RenderingHandlerRegistry
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.display2.implementation.common.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.ExtraLore
import cc.mewcraft.wakame.item2.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.hideAll
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class SimpleRendererFormatRegistry : AbstractRendererFormatRegistry(SimpleItemRenderer)

internal class SimpleItemRendererLayout : AbstractRendererLayout(SimpleItemRenderer)

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object SimpleItemRenderer : AbstractItemRenderer<Nothing>() {
    override val name: String = "simple"
    override val formats: AbstractRendererFormatRegistry = SimpleRendererFormatRegistry()
    override val layout: AbstractRendererLayout = SimpleItemRendererLayout()
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
        SimpleRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: Nothing?) {
        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        item.process(ItemMetaTypes.CUSTOM_NAME) { data -> SimpleRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.ITEM_NAME) { data -> SimpleRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }

        item.process(ItemPropertyTypes.EXTRA_LORE) { data -> SimpleRenderingHandlerRegistry.LORE.process(collector, data) }

        val lore = textAssembler.assemble(collector)
        item.fastLore(lore)

        item.hideAll()
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

