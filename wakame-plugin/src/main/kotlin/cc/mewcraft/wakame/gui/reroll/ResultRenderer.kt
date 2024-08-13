package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.hideTooltip
import me.lucko.helper.text3.mini
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 负责渲染重造后的物品在 [RerollingMenu.outputInventory] 里面的样子.
 */
internal object ResultRenderer {

    // TODO 更丰富的结果预览:
    //  能够显示哪些词条栏会被重造, 哪些不会
    //  这要求对渲染模块进行重构 ...

    fun render(result: RerollingSession.Result): ItemStack {
        val item = result.item // deep clone
        val ret: ItemStack

        if (result.successful) {
            ret = item.unsafe.handle
            ret.editMeta { meta ->
                val lore = buildList {
                    add("<!i><white>重造花费: <green>${result.cost.default}".mini)
                }
                meta.lore(lore)
            }
        } else {
            ret = ItemStack(Material.BARRIER).hideTooltip(true)
        }

        return ret
    }
}
