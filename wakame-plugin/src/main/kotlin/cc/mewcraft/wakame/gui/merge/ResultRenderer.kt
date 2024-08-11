package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.bypassPacket
import cc.mewcraft.wakame.reforge.merge.MergingSession
import net.kyori.adventure.text.Component

/**
 * 负责渲染合并后的结果.
 */
internal class ResultRenderer(
    private val result: MergingSession.Result,
) : ItemRenderer<NekoStack> {
    override fun render(nekoStack: NekoStack) {
        nekoStack.bypassPacket()

        val handle = nekoStack.unsafe.handle

        handle.editMeta { meta ->
            val name = Component.text("合成核心")
            meta.itemName(name)

            val lore = mutableListOf<Component>()
            lore += Component.empty()
            lore += result.type.description
            lore += Component.empty()
            lore += result.cost.description
            meta.lore(lore)
        }

        nekoStack.erase()
    }
}