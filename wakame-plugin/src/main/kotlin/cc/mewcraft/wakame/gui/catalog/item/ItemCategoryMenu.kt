package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.item.ItemStacks
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 类别菜单.
 * 展示某类别的所有物品.
 */
class ItemCategoryMenu(
    /**
     * 该菜单展示的 [Category].
     */
    val category: Category,

    /**
     * 该菜单的上一级菜单.
     * 即物品图鉴主菜单.
     */
    val parent: ItemCatalogMainMenu,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) {
    private val settings = category.menuSettings

    /**
     * 菜单的 [Gui].
     *
     * - `x`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     * - `.`: display
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('b', BackItem())
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        // TODO 对随机物品的特殊渲染
        // TODO 可以缓存，只有重载时会变化
        // 类别菜单所展示的物品
        // 无法创建则显示错误占位物品
        builder.setContent(category.items.map { itemX -> SimpleItem(itemX.createItemStack() ?: ItemStacks.createUnknown(itemX.identifier)) })
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        setTitle(settings.title)
    }

    fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
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
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return settings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("back").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            parent.open()
        }
    }
}