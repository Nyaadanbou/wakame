/**
 * 有关*重造台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class RerollingTableRendererFormats(renderer: RerollingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class RerollingTableRendererLayout(renderer: RerollingTableItemRenderer) : AbstractRendererLayout(renderer)

internal class RerollingTableContext

internal object RerollingTableItemRenderer : AbstractItemRenderer<NekoStack, RerollingTableContext>() {
    override val name: String = "rerolling_table"
    override val rendererFormats = RerollingTableRendererFormats(this)
    override val rendererLayout = RerollingTableRendererLayout(this)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 RerollingTable
    }

    override fun render(item: NekoStack, context: RerollingTableContext?) {
        // TODO display2 RerollingTable
    }
}
