package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.reforge.merge.MergingSession
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 负责渲染合并后的物品在 [MergingMenu.outputSlot] 里面的样子.
 */
internal object ResultRenderer {
    fun render(result: MergingSession.Result): ItemStack {
        val item = result.item
        val ret: ItemStack

        if (result.successful) {
            // 渲染成功的结果

            ret = ItemStack(item.unsafe.handle.type)
            ret.editMeta { meta ->
                val name = text {
                    content("合成核心")
                    color(NamedTextColor.AQUA)
                    decorate(TextDecoration.BOLD)
                }
                val lore = buildList {
                    add(Component.empty())
                    addAll(result.type.description)
                    addAll(result.cost.description)
                }

                meta.itemName(name)
                meta.lore(lore)
            }
        } else {
            // 渲染失败的结果

            ret = ItemStack(Material.BARRIER) // 使用 `minecraft:barrier` 作为合并失败的“基础物品”
            ret.editMeta { meta ->
                val name = text {
                    content("无法合并!")
                    color(NamedTextColor.RED)
                    decorate(TextDecoration.BOLD)
                }
                val lore = buildList {
                    add(Component.empty())
                    addAll(result.type.description)
                    addAll(result.cost.description)
                }

                meta.itemName(name)
                meta.lore(lore)
            }
        }

        return ret
    }
}
