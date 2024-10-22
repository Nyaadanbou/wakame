package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.hideAllFlags
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * 重造台中用于选择*单个*核孔的子菜单, 将被嵌入进 [RerollingMenu] 中.
 */
internal class SelectionMenu
private constructor(
    val parent: RerollingMenu,
    val selection: RerollingSession.Selection,
) {

    companion object {
        operator fun invoke(parent: RerollingMenu, selection: RerollingSession.Selection): Gui {
            return SelectionMenu(parent, selection).primaryGui
        }
    }

    private val primaryGui: Gui = Gui.normal { builder ->
        builder.setStructure(
            "a",
            "b"
        )
        builder.addIngredient('a', IndicatorItem())
        builder.addIngredient('b', SelectionItem())
    }

    /**
     * 用于给玩家展示核孔的样子.
     */
    private inner class IndicatorItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(selection.display)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }

    /**
     * 用于给玩家选择是否重造核孔.
     */
    private inner class SelectionItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val mat: Material
            val name: Component

            if (selection.selected) {
                mat = Material.PINK_DYE
                name = text("将被重造")
            } else {
                mat = Material.GRAY_DYE
                name = text("保持不变")
            }

            val stack = ItemStack(mat).hideAllFlags()
            stack.editMeta { it.itemName(name) }

            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (selection.invert()) {
                parent.viewer.sendPlainMessage("核孔 ${selection.id} 将被重造.")
            } else {
                parent.viewer.sendPlainMessage("核孔 ${selection.id} 保持不变.")
            }

            // 执行一次重造, 并更新主菜单
            parent.executeReforge()
            parent.updateOutput()

            notifyWindows()
        }
    }
}