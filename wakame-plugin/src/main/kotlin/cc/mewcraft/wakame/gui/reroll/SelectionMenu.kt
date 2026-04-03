package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item

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

    private val primaryGui = Gui.builder()
        .setStructure(*parent.table.selectionMenuSettings.structure)
        // a: 给玩家展示核孔的样子
        .addIngredient(
            'a', Item.builder()
                .setItemProvider { _ ->
                    val sourceItem = parent.session.usableInput ?: return@setItemProvider parent.table.selectionMenuSettings.getIcon("error").resolveToItemWrapper()
                    val sourceCore = sourceItem.getData(ItemDataTypes.CORE_CONTAINER)?.get(selection.id) ?: return@setItemProvider parent.table.selectionMenuSettings.getIcon("error").resolveToItemWrapper()
                    parent.table.selectionMenuSettings.getIcon("core_view").resolveToItemWrapper {
                        standard { component("core_name", sourceCore.displayName) }
                        folded("core_description", sourceCore.description)
                    }
                }
        )
        // b: 给玩家选择是否重造核孔
        .addIngredient(
            'b',
            Item.builder()
                .setItemProvider { _ ->
                    val slotDisplayId = if (selection.selected) "core_selected" else "core_unselected"
                    parent.table.selectionMenuSettings.getIcon(slotDisplayId).resolveToItemWrapper()
                }
                .addClickHandler { item, _ ->
                    selection.invert()

                    // 执行一次重造, 并更新主菜单
                    parent.confirmed = false
                    parent.executeReforge()
                    parent.updateInputSlot()
                    parent.updateOutputSlot()

                    item.notifyWindows()
                }
        )
        .build()
}