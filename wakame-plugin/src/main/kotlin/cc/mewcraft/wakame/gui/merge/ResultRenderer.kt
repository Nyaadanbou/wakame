package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 负责渲染合并后的物品在 [MergingMenu.outputSlot] 里面的样子.
 */
internal object ResultRenderer {
    fun render(result: MergingSession.Result): ItemStack {
        val item = result.item
        val ret: ItemStack

        val clickToMerge = "<gray>⤷ 点击确认合并".mini

        if (result.successful) {
            // 渲染成功的结果

            ret = ItemStack(item.itemStack.type)
            ret.editMeta { meta ->
                val name = "<white>结果: <green>就绪".mini
                val lore = buildList<Component> {
                    add(Component.empty())
                    add(result.description)
                    add(Component.empty())
                    addAll(result.type.description)
                    addAll(result.cost.description)
                    add(Component.empty())
                    add(clickToMerge)
                }.removeItalic

                meta.displayName(name.removeItalic)
                meta.lore(lore)
            }
        } else {
            // 渲染失败的结果

            ret = ItemStack(Material.BARRIER) // 使用 `minecraft:barrier` 作为合并失败的“基础物品”
            ret.editMeta { meta ->
                val name = "<white>结果: <red>失败".mini
                val lore = buildList<Component> {
                    add(Component.empty())
                    add(result.description)
                    add(Component.empty())
                    addAll(result.type.description)
                    addAll(result.cost.description)
                }.removeItalic

                meta.itemName(name)
                meta.lore(lore)
            }
        }

        return ret
    }
}
