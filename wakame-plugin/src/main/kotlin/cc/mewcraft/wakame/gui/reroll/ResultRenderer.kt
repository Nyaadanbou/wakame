package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 负责渲染重造后的物品在 [RerollingMenu.outputSlot] 里面的样子.
 */
internal object ResultRenderer {

    // TODO 更丰富的结果预览:
    //  能够显示哪些词条栏会被重造, 哪些不会
    //  这要求对渲染模块进行重构 ...

    fun render(result: RerollingSession.Result): ItemStack {
        val item = result.item // deep clone
        val ret: ItemStack

        if (result.successful) {
            item.erase() // FIXME NekoStack#gui

            // 在原物品的基础上做修改
            ret = item.itemStack
            ret.editMeta { meta ->
                val name = "<white>重造结果: <green>准备就绪".mini
                val lore = buildList {
                    add("<white>重造花费: <green>${result.cost.default}".mini)
                    add(Component.empty())
                    add("<gray>⤷ 点击取出".mini)
                }.removeItalic

                meta.itemName(name)
                meta.lore(lore)
            }
        } else {

            // 在新创建的物品上做修改
            ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<white>重造结果: <red>失败".mini
                val lore = buildList {
                    addAll(result.description)
                }.removeItalic

                meta.itemName(name)
                meta.lore(lore)
            }
        }

        return ret
    }
}
