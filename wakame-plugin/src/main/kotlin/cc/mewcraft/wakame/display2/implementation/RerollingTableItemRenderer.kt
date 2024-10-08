/**
 * 有关*重造台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class RerollingTableContext

internal object RerollingTableItemRenderer: AbstractItemRenderer<NekoStack, RerollingTableContext>() {
    override val rendererLayout: RendererLayout
        get() = TODO("Not yet implemented")
    override val rendererFormats: RendererFormats
        get() = TODO("Not yet implemented")

    override fun initialize(layoutPath: Path, formatPath: Path) {
        TODO("Not yet implemented")
    }

    override fun render(item: NekoStack, context: RerollingTableContext?) {
        TODO("Not yet implemented")
    }
}
