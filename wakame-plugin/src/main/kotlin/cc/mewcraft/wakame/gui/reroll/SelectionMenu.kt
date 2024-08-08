package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.util.hideAllFlags
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
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
internal class SelectionMenu(
    val parentMenu: RerollingMenu,
    val selection: RerollingSession.Selection,
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
        val trims: List<Material> = Tag.ITEMS_TRIM_TEMPLATES.values.toList()
    }

    private inner class IndicatorItem : AbstractItem() {
        fun getTrimMaterial(): Material {
            val sessionHash = selection.hashCode()
            val index = sessionHash % trims.size
            return trims[index]
        }

        override fun getItemProvider(): ItemProvider {
            val stack = ItemStack(getTrimMaterial()).hideAllFlags()
            selection.display.apply(stack)
            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // NOP
        }
    }

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
            stack.editMeta { meta -> meta.itemName(name) }

            return ItemWrapper(stack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (parentMenu.rerollingSession == null) {
                logger.error("Trying to select without a session. This is a bug!")
                return
            }

            selection.invertSelect()

            if (selection.selected) {
                parentMenu.viewer.sendPlainMessage("词条栏 ${selection.id} 将被重造.")
            } else {
                parentMenu.viewer.sendPlainMessage("词条栏 ${selection.id} 保持不变.")
            }

            // 重新渲染主菜单的输出物品
            parentMenu.refreshOutput()
        }
    }
}