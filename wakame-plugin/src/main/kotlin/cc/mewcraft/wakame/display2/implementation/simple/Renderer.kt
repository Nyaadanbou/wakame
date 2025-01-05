package cc.mewcraft.wakame.display2.implementation.simple

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.display2.implementation.common.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import java.nio.file.Path

internal class SimpleRendererFormatRegistry : AbstractRendererFormatRegistry(SimpleItemRenderer)

internal class SimpleItemRendererLayout : AbstractRendererLayout(SimpleItemRenderer)

internal data object SimpleItemRendererContext

@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    order = ReloadableOrder.NORMAL
)
internal object SimpleItemRenderer : AbstractItemRenderer<NekoStack, SimpleItemRendererContext>() {
    override val name: String = "simple"
    override val formats: AbstractRendererFormatRegistry = SimpleRendererFormatRegistry()
    override val layout: AbstractRendererLayout = SimpleItemRendererLayout()
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        SimpleRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: SimpleItemRendererContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> SimpleRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> SimpleRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> SimpleRenderingHandlerRegistry.LORE.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)

        item.unsafeEdit {
            lore = itemLore
            showNothing()
        }
    }

    @InitFun
    fun onPostWorld() {
        initialize0()
    }

    @ReloadableFun
    fun onReload() {
        initialize0()
    }
}

internal object SimpleRenderingHandlerRegistry : RenderingHandlerRegistry(SimpleItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingHandler<CustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ITEM_NAME: RenderingHandler<ItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)
}

