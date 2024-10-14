/**
 * 有关*合成站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.item.NekoStack
import java.nio.file.Path

/**
 * @param pos 物品出现的位置
 */
internal data class CraftingStationContext(
    val pos: Pos,
) {
    enum class Pos {
        OVERVIEW, // 合成站的主要菜单
        PREVIEW, // 合成站的预览菜单
        CHOICE, // station choice
        RESULT, // station result
    }
}

internal object CraftingStationItemRenderer : AbstractItemRenderer<NekoStack, CraftingStationContext>() {
    override val name: String = "crafting_station"
    override val rendererLayout: RendererLayout
        get() = TODO("Not yet implemented")
    override val rendererFormats: RendererFormats
        get() = TODO("Not yet implemented")

    override fun initialize(formatPath: Path, layoutPath: Path) {
        // TODO display2 CraftingStation
    }

    override fun render(item: NekoStack, context: CraftingStationContext?) {
        item.isClientSide = false
        // TODO display2 继续写完
    }
}
