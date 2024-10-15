/**
 * 有关*合并台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class MergingTableRendererFormats(renderer: MergingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class MergingTableRendererLayout(renderer: MergingTableItemRenderer) : AbstractRendererLayout(renderer)

internal class MergingTableContext

internal object MergingTableItemRenderer : AbstractItemRenderer<NekoStack, MergingTableContext>() {
    override val name: String = "merging_table"
    override val formats = MergingTableRendererFormats(this)
    override val layout = MergingTableRendererLayout(this)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 MergingTable
    }

    override fun render(item: NekoStack, context: MergingTableContext?) {
        // TODO display2 MergingTable
    }
}
