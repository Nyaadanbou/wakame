/**
 * 有关*定制台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

internal class ModdingTableContext

internal object ModdingTableItemRenderer : AbstractItemRenderer<NekoStack, ModdingTableContext>() {
    override var rendererLayout: RendererLayout
        get() = TODO("Not yet implemented")
        set(value) {}
    override var rendererFormats: RendererFormats
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun initialize(layoutPath: Path, formatPath: Path) {
        TODO("Not yet implemented")
    }

    override fun render(item: NekoStack, context: ModdingTableContext?) {
        TODO("Not yet implemented")
    }
}
