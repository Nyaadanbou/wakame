package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.display.resolveToItemWrapper
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
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
        builder.setStructure(*parent.table.selectionMenuSettings.structure)
        builder.addIngredient('a', IndicatorItem())
        builder.addIngredient('b', SelectionItem())
    }

    /**
     * 用于给玩家展示核孔的样子.
     */
    private inner class IndicatorItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val sourceItem = parent.session.usableInput ?: return parent.table.selectionMenuSettings.getSlotDisplay("error").resolveToItemWrapper()
            val sourceCore = sourceItem.getData(ItemDataTypes.CORE_CONTAINER)?.get(selection.id) ?: return parent.table.selectionMenuSettings.getSlotDisplay("error").resolveToItemWrapper()
            return parent.table.selectionMenuSettings.getSlotDisplay("core_view").resolveToItemWrapper {
                standard { component("core_name", sourceCore.displayName) }
                folded("core_description", sourceCore.description)
            }
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
            val slotDisplayId = if (selection.selected) "core_selected" else "core_unselected"
            return parent.table.selectionMenuSettings.getSlotDisplay(slotDisplayId).resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            selection.invert()

            // 执行一次重造, 并更新主菜单
            parent.confirmed = false
            parent.executeReforge()
            parent.updateInputSlot()
            parent.updateOutputSlot()

            notifyWindows()
        }
    }
}