package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.catalog.item.CategoryRegistry
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings.SlotDisplay
import cc.mewcraft.wakame.registry2.KoishRegistries
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
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 物品图鉴主菜单.
 * 展示所有的物品类别.
 */
class ItemCatalogMainMenu(
    /**
     * 该菜单的布局
     */
    val settings: BasicMenuSettings,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) {

    /**
     * 菜单的 [Gui].
     *
     * - `x`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `s`: search
     * - `.`: category
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('s', SearchItem())
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        // TODO 类别的图标可以缓存，重载才需要变化
        builder.setContent(CategoryRegistry.getCategoryMap().values.map { category -> CategoryItem(category) })
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
     * **搜索** 的图标.
     */
    inner class SearchItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("search").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            //TODO 物品搜索功能
        }
    }

    /**
     * **类别** 的图标.
     * 点击后打开一个特定的类别菜单.
     */
    inner class CategoryItem(
        private val category: Category
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val itemId = category.icon
            val itemEntry = KoishRegistries.ITEM.getEntry(itemId) ?: run {
                LOGGER.warn("Menu icon of category '${category.id}' with item ID '$itemId' not found in the item registry, using default icon")
                KoishRegistries.ITEM.getDefaultEntry()
            }
            return SlotDisplay(itemEntry).resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            ItemCategoryMenu(category, this@ItemCatalogMainMenu, viewer).open()
        }
    }
}