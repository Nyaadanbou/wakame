package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.catalog.item.ItemCatalogManager
import cc.mewcraft.wakame.core.ItemX
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 类别菜单.
 * 展示某类别的所有物品.
 */
class CategoryMenu(
    /**
     * 该菜单展示的 [Category].
     */
    val category: Category,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) : ItemCatalogMenu {
    private val settings = category.menuSettings

    /**
     * 菜单的 [Gui].
     *
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     * - `x`: display
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('b', BackItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        // TODO 可以缓存, 只有重载时会变化
        // 类别菜单所展示的物品
        builder.setContent(category.items.map { itemX -> DisplayItem(itemX) })
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        setTitle(settings.title)
    }

    override fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("background").resolveToItemWrapper()
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
    }

    /**
     * 上一页的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider = settings.getSlotDisplay("prev_page").resolveToItemWrapper()
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider = settings.getSlotDisplay("next_page").resolveToItemWrapper()
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("back").resolveToItemWrapper()
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val menuStack = ItemCatalogManager.getMenuStack(viewer)
            menuStack.removeFirst()
            menuStack.first.open()
        }
    }

    /**
     * **展示物品** 的图标.
     */
    inner class DisplayItem(
        val item: ItemX,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            // TODO 渲染
            return ItemWrapper(item.createItemStack())
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 类别菜单无需判定是否套娃, 因为肯定是第一次对配方进行索引
            val lookupState = when (clickType) {
                ClickType.LEFT, ClickType.RIGHT -> LookupState.SOURCE
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> LookupState.USAGE
                else -> return
            }
            // 要打开的菜单列表为空, 则不打开
            val catalogRecipeGuis = CatalogRecipeGuis.createGuis(item, lookupState)
            if (catalogRecipeGuis.isEmpty()) return

            val pagedCatalogRecipesMenu = PagedCatalogRecipesMenu(item, lookupState, player, catalogRecipeGuis)
            ItemCatalogManager.getMenuStack(player).addFirst(pagedCatalogRecipesMenu)
            pagedCatalogRecipesMenu.open()
        }
    }
}