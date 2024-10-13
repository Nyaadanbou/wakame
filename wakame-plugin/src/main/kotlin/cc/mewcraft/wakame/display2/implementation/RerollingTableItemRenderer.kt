/**
 * 有关*重造台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class RerollingTableContext

internal object RerollingTableItemRenderer : AbstractItemRenderer<NekoStack, RerollingTableContext>() {
    override val name: String = "rerolling_table"
    override val rendererLayout: RendererLayout
        get() = TODO("display2 RerollingTable")
    override val rendererFormats: RendererFormats
        get() = TODO("display2 RerollingTable")

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 RerollingTable
    }

    override fun render(item: NekoStack, context: RerollingTableContext?) {
        // TODO display2 RerollingTable
    }
}
