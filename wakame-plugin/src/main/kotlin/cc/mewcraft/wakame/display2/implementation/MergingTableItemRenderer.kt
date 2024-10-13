/**
 * 有关*合并台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class MergingTableContext

internal object MergingTableItemRenderer : AbstractItemRenderer<NekoStack, MergingTableContext>() {
    override var rendererLayout: RendererLayout
        get() = TODO("display2 MeringTable")
        set(value) {}
    override var rendererFormats: RendererFormats
        get() = TODO("display2 MeringTable")
        set(value) {}

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 MergingTable
    }

    override fun render(item: NekoStack, context: MergingTableContext?) {
        // TODO display2 MergingTable
    }
}
