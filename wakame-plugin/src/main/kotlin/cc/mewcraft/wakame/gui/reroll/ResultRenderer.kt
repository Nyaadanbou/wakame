package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 负责渲染 [RerollingMenu.outputSlot] 里面的内容.
 */
internal object ResultRenderer {

    // TODO 更丰富的结果预览:
    //  能够显示哪些词条栏会被重造, 哪些不会
    //  这要求对渲染模块进行重构 ...

    fun render(session: RerollingSession): ItemStack {
        val viewer = session.viewer
        val result = session.latestResult
        val item = result.item // deep clone

        if (result.successful) {
            // 如果可以重造:

            if (!result.cost.test(viewer)) {
                // 如果玩家没有足够的资源:

                val ret = ItemStack(Material.BARRIER)
                ret.editMeta { meta ->
                    val name = "<white>结果: <red>资源不足".mini
                    val lore = buildList<Component> {
                        addAll(result.description)
                        addAll(result.cost.description)
                    }

                    meta.itemName(name)
                    meta.lore(lore.removeItalic)
                }

                return ret
            }

            // 移除物品的萌芽数据
            item.erase() // FIXME NekoStackDisplay

            // 在原物品的基础上做修改
            // 这样可以保留物品的类型以及其他的原版组件信息
            val ret = item.itemStack
            ret.editMeta { meta ->
                val name = "<white>结果: <green>就绪".mini
                val lore = buildList<Component> {
                    addAll(result.description)
                    addAll(result.cost.description)
                    add(Component.empty())
                    add("<gray>⤷ 点击确认重造".mini)
                }

                meta.displayName(name.removeItalic)
                meta.lore(lore.removeItalic)
            }

            return ret

        } else {
            // 如果不可重造:

            // 在新创建的物品上做修改
            // 因为根本无法重造所以原物品的信息就无所谓了
            val ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<white>结果: <red>失败".mini
                val lore = buildList<Component> {
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore.removeItalic)
            }

            return ret
        }
    }
}
