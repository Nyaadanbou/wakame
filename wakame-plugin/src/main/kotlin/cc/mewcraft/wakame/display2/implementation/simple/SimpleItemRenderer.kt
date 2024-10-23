package cc.mewcraft.wakame.display2.implementation.simple

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.directEdit
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.*
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import java.nio.file.Path

internal class SimpleItemRendererFormats : AbstractRendererFormats(SimpleItemRenderer)
internal class SimpleItemRendererLayout : AbstractRendererLayout(SimpleItemRenderer)

internal class SimpleItemRenderContext

internal object SimpleItemRenderer : AbstractItemRenderer<NekoStack, SimpleItemRenderContext>() {
    override val name: String = "simple"
    override val formats: AbstractRendererFormats = SimpleItemRendererFormats()
    override val layout: AbstractRendererLayout = SimpleItemRendererLayout()
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        SimpleItemRendererParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: SimpleItemRenderContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> SimpleItemRendererParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> SimpleItemRendererParts.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> SimpleItemRendererParts.LORE.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)
        val itemCmd = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.directEdit {
            lore = itemLore
            customModelData = itemCmd
            showNothing()
        }
    }
}

internal object SimpleItemRendererParts : RenderingParts(SimpleItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingParts.LORE(this)
}

