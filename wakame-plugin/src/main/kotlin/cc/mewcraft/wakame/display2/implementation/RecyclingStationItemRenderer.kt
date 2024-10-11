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
    override var rendererLayout: RendererLayout
        get() = TODO("display2 RecyclingStation")
        set(value) {}
    override var rendererFormats: RendererFormats
        get() = TODO("display2 RecyclingStation")
        set(value) {}

    override fun initialize(layoutPath: Path, formatPath: Path) {
        // TODO display2 RecyclingStation
    }

    override fun render(item: NekoStack, context: RecyclingStationContext?) {
        // TODO display2 RecyclingStation
    }
}
