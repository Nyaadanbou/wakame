/**
 * 有关*收购站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.recycling_station

import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.templates.components.*
import java.nio.file.Path

internal class RecyclingStationRendererFormats(renderer: RecyclingStationItemRenderer) : AbstractRendererFormats(renderer)
internal class RecyclingStationRendererLayout(renderer: RecyclingStationItemRenderer) : AbstractRendererLayout(renderer)
internal class RecyclingStationContext

internal object RecyclingStationItemRenderer : AbstractItemRenderer<NekoStack, RecyclingStationContext>() {
    override val name: String = "recycling_station"
    override val formats = RecyclingStationRendererFormats(this)
    override val layout: AbstractRendererLayout = RecyclingStationRendererLayout(this)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RecyclingStationItemRendererParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: RecyclingStationContext?) {
        requireNotNull(context) { "context" }
    }
}

internal object RecyclingStationItemRendererParts : RenderingParts(RecyclingStationItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingParts.LORE(this)
}