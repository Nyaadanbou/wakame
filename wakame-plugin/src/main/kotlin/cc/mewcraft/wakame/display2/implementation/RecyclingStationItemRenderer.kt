/**
 * 有关*收购站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class RecyclingStationContext

internal object RecyclingStationItemRenderer : AbstractItemRenderer<NekoStack, RecyclingStationContext>() {
    override val rendererLayout: RendererLayout
        get() = TODO()
    override val rendererFormats: RendererFormats
        get() = TODO()

    override fun initialize(layoutPath: Path, formatPath: Path) {
        TODO()
    }

    override fun render(item: NekoStack, context: RecyclingStationContext?) {
        TODO()
    }
}
