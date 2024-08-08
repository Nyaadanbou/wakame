package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.bypassPacket
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.TextDecoration

/**
 * 用于渲染重造后的物品.
 */
// TODO 更丰富的结果预览:
//  能够显示哪些词条栏会被重造, 哪些不会
//  这要求对渲染模块进行重构 ...
internal class ResultItemRenderer(
    private val result: RerollingSession.Result,
) : ItemRenderer<NekoStack> {
    override fun render(nekoStack: NekoStack) {
        // 关闭发包渲染
        nekoStack.bypassPacket(true)

        // 渲染重造的总花费
        nekoStack.unsafe.handle.editMeta { meta ->
            val lore = text {
                content("重造花费: ${result.cost.default}")
                decoration(TextDecoration.ITALIC, false)
            }
            meta.lore(listOf(lore))
        }

        nekoStack.erase()
    }
}
