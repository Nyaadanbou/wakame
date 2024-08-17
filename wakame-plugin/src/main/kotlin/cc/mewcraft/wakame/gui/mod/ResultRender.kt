package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.reforge.mod.ModdingSession
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
 */
internal object ResultRender {
    fun renderConfirm(result: ModdingSession.Result): ItemStack {
        val ret: ItemStack

        ret = ItemStack(Material.ANVIL)
        ret.editMeta { meta ->
            val name = "<green>再次点击确认取出".mini
            val lore = buildList {
                add(Component.empty())
                addAll(result.description)
            }

            meta.itemName(name)
            meta.lore(lore)
        }

        return ret
    }

    /**
     * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
     *
     * @param result 定制结果
     * @return 渲染后的物品
     */
    fun renderNormal(result: ModdingSession.Result): ItemStack {
        val item = result.outputItem
        val ret: ItemStack

        if (result.successful) {
            // 定制成功了

            if (item == null) {
                val error = ItemStack(Material.BARRIER).apply {
                    editMeta { meta ->
                        meta.itemName("<red>内部错误".mini)
                    }
                }
                return error
            }

            // FIXME 移除萌芽标签 / 实现 NekoStack#gui
            ret = item.itemStack
            ret.editMeta { meta ->
                val name = "<green>定制结果 (成功)".mini
                val lore = buildList {
                    add(Component.empty())
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore)
            }
        } else {
            // 定制失败了

            ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<red>定制结果 (失败)".mini
                val lore = buildList {
                    add(Component.empty())
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore)
            }
        }

        return ret
    }
}