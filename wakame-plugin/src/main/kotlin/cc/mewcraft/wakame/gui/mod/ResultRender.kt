package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
 *
 * - 渲染普通图标, 使用 [ResultRender.normal].
 * - 渲染确认图标, 使用 [ResultRender.confirm].
 */
internal object ResultRender {
    /**
     * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
     *
     * @param result 定制的结果
     * @return 渲染后的物品
     */
    fun confirm(result: ModdingSession.Result): ItemStack {
        val ret = ItemStack(Material.ANVIL)
        ret.editMeta { meta ->
            val name = "<white>再次点击确认取出".mini
            meta.itemName(name)
        }

        return ret
    }

    /**
     * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
     *
     * @param result 定制的结果
     * @return 渲染后的物品
     */
    fun normal(result: ModdingSession.Result): ItemStack {
        val item = result.outputItem
        val ret: ItemStack

        if (result.successful) {
            // 定制成功了

            if (item == null) {
                ret = ItemStack(Material.BARRIER)
                ret.editMeta { meta ->
                    val name = "<white>结果: <red>内部错误".mini
                    meta.itemName(name)
                }
            } else {
                // FIXME 移除萌芽标签 / 实现 NekoStack#gui
                ret = item.itemStack
                ret.editMeta { meta ->
                    val name = "<white>结果: <green>准备就绪".mini
                    val lore = buildList {
                        addAll(result.description)
                    }

                    meta.itemName(name)
                    meta.lore(lore.removeItalic)
                }
            }
        } else {
            // 定制失败了

            ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<white>结果: <red>失败".mini
                val lore = buildList {
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore.removeItalic)
            }
        }

        return ret
    }
}