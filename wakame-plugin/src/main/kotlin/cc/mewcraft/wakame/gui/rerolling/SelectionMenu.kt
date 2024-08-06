package cc.mewcraft.wakame.gui.rerolling

import cc.mewcraft.wakame.reforge.rerolling.RerollingSession
import cc.mewcraft.wakame.util.hideAllFlags
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * 重造台中用于选择*单个*词条栏的子菜单, 将被嵌入进 [RerollingMenu] 中.
 */
class SelectionMenu(
    val parentMenu: RerollingMenu,
    val selectionSession: RerollingSession.SelectionSession,
) : KoinComponent {
    val primaryGui: Gui = Gui.normal { builder ->
        builder.setStructure(
            "a",
            "b"
        )
        builder.addIngredient('a', IndicatorItem())
        builder.addIngredient('b', SelectionItem())
    }

    private val logger: Logger by inject()

    private companion object {
        // 临时实现
        val trims: List<Material> = Tag.ITEMS_TRIM_TEMPLATES.values.toList()
    }

    private inner class IndicatorItem : AbstractItem() {
        fun getTrimMaterial(): Material {
            val sessionHash = selectionSession.hashCode()
            val index = sessionHash % trims.size
            return trims[index]
        }

        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(getTrimMaterial()).hideAllFlags()
            selectionSession.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }

    private inner class SelectionItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val stack = if (selectionSession.selected) {
                // 临时实现, 表示已选择
                Material.PINK_DYE
            } else {
                // 临时实现, 表示未选择
                Material.GRAY_DYE
            }.let {
                ItemStack(it).hideAllFlags().hideTooltip(true)
            }
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (parentMenu.rerollingSession == null) {
                logger.error("Trying to select without a session. This is a bug!")
                return
            }

            // 反转当前的选择状态
            selectionSession.selected = !selectionSession.selected
        }
    }
}