/**
 * 有关*定制台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class ModdingTableRendererFormats(renderer: ModdingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class ModdingTableRendererLayout(renderer: ModdingTableItemRenderer) : AbstractRendererLayout(renderer)

internal class ModdingTableContext

internal object ModdingTableItemRenderer : AbstractItemRenderer<NekoStack, ModdingTableContext>() {
    override val name: String = "modding_table"
    override val formats = ModdingTableRendererFormats(this)
    override val layout = ModdingTableRendererLayout(this)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 ModdingTable
    }

    override fun render(item: NekoStack, context: ModdingTableContext?) {
        // TODO display2 ModdingTable
        // Replace #display
        // Result #display
    }
}
