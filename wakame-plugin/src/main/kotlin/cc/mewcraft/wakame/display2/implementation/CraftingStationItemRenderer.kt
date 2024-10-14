/**
 * 有关*合成站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.IndexedTextFlatter
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.nio.file.Path

internal class CraftingStationRendererFormats(renderer: CraftingStationItemRenderer) : AbstractRendererFormats(renderer)

internal class CraftingStationRendererLayout(renderer: CraftingStationItemRenderer) : AbstractRendererLayout(renderer)

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
    override val rendererFormats = CraftingStationRendererFormats(this)
    override val rendererLayout = CraftingStationRendererLayout(this)
    private val indexedTextFlatter = IndexedTextFlatter(rendererLayout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingParts.bootstrap()
        rendererFormats.initialize(formatPath)
        rendererLayout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ObjectArrayList<IndexedText>()

        val templates = item.templates

        val components = item.components


        // TODO display2 继续写完
    }

    private inline fun <T> ItemTemplateMap.process(type: ItemTemplateType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }

    private inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }
}


//////


internal object CraftingStationRenderingParts : RenderingParts() {

}


//////


//<editor-fold desc="RendererFormat">
//</editor-fold>


//////


//<editor-fold desc="TextMeta">
//</editor-fold>