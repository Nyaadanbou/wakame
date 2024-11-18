package cc.mewcraft.wakame.gui.guidebook.menu

import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.gui.getFixedIconItemProvider
import cc.mewcraft.wakame.gui.guidebook.Category
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window

/**
 * 类别菜单.
 * 展示某类别的所有物品.
 */
class CategoryMenu(
    val category: Category,
    val layout: MenuLayout,
    val parent: GuideBookMainMenu
) {

    /**
     * 菜单的 [Gui].
     *
     * - `X`: background
     * - `.`: category
     * - `<`: prev_page
     * - `>`: next_page
     * - `B`: back_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*layout.structure)
        builder.addIngredient('X', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('B', BackItem())
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        addOpenHandler(::onWindowOpen)
        addCloseHandler(::onWindowClose)
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    /**
     * 向指定玩家打开合成站的主界面.
     */
    fun open() {
        primaryWindow.open(viewer)
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return layout.getFixedIconItemProvider("background")
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // do nothing
        }
    }

    /**
     * 上一页的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return layout.getFixedIconItemProvider("background")
            }
            return layout.getFixedIconItemProvider("prev_page")
        }
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return layout.getFixedIconItemProvider("background")
            }
            return layout.getFixedIconItemProvider("next_page")
        }
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return layout.getFixedIconItemProvider("back")
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            parent.open()
        }
    }
}