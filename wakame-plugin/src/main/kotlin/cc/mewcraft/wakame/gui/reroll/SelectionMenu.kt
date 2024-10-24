package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.getCell
import cc.mewcraft.wakame.reforge.common.CoreIcons
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.edit
import cc.mewcraft.wakame.util.removeItalic
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
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
     * @param id 核孔的 id
     */
    private fun getCoreDisplayNameBy(id: String): Component {
        return parent.session.sourceItem?.getCell(id)?.getCore()?.displayName
            ?: return text("内部错误 (no such cell)").color(NamedTextColor.AQUA)
    }

    /**
     * @param message 错误信息
     */
    private fun makeErrorItem(message: Component): ItemProvider {
        return ItemWrapper(ItemStack.of(Material.BARRIER).edit { itemName = message })
    }

    /**
     * 用于给玩家展示核孔的样子.
     */
    private inner class IndicatorItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val session = parent.session
            val sourceItem = session.sourceItem ?: return makeErrorItem(text("内部错误 (source item is null)"))
            val cellId = selection.id
            val cell = sourceItem.getCell(cellId) ?: return makeErrorItem(text("内部错误 (cell '$cellId' is null)"))
            val core = cell.getCore()
            val icon = CoreIcons.get(core).edit {
                itemName = core.displayName
                lore = buildList {
                    add(text {
                        content("惩罚: ").color(NamedTextColor.GRAY)
                        append(text(cell.getReforgeHistory().rerollCount).color(NamedTextColor.RED))
                    })
                    addAll(core.description)
                }.removeItalic
            }

            return ItemWrapper(icon)
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
            val type: Material
            val name: Component

            if (selection.selected) {
                type = Material.PINK_DYE
                name = text("将被重造")
            } else {
                type = Material.GRAY_DYE
                name = text("保持不变")
            }

            val stack = ItemStack.of(type).edit {
                itemName = name
            }

            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val selectedId = selection.id
            if (selection.invert()) {
                parent.viewer.sendMessage(text {
                    content("核孔 [")
                    append(getCoreDisplayNameBy(selectedId))
                    append(text("] 将被重造."))
                })
            } else {
                parent.viewer.sendMessage(text {
                    content("核孔 [")
                    append(getCoreDisplayNameBy(selectedId))
                    append(text("] 将保持不变."))
                })
            }

            // 执行一次重造, 并更新主菜单
            parent.executeReforge()
            parent.updateOutput()

            notifyWindows()
        }
    }
}