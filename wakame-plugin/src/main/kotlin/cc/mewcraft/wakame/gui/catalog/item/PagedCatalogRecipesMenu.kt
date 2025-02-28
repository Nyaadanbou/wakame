package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.ItemCatalogInitializer
import cc.mewcraft.wakame.catalog.item.ItemCatalogManager
import cc.mewcraft.wakame.core.ItemX
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 聚焦于一个物品, 展示其来源/用途的菜单.
 */
internal class PagedCatalogRecipesMenu(
    /**
     * 该菜单聚焦的物品.
     */
    val item: ItemX,

    /**
     * 该菜单的检索状态.
     */
    val lookupState: LookupState,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,

    /**
     * 该菜单中的各页配方 [Gui].
     */
    val catalogRecipeGuis: List<Gui>,
) : ItemCatalogMenu {

    private val settings = ItemCatalogInitializer.getMenuSettings("paged_catalog_recipes")

    /**
     * 菜单的 [Gui].
     *
     * - `x`: catalog_recipe_gui
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     */
    private val primaryGui: PagedGui<Gui> = PagedGui.guis { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('b', BackItem())
        builder.setContent(catalogRecipeGuis)
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        // TODO 标题根据 CatalogRecipeGui 的设置而变化
        setTitle(settings.title)
    }

    override fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * **背景** 占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("background").resolveToItemWrapper()
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
    }

    /**
     * **上一页** 的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    /**
     * **下一页** 的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasNextPage()) {
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return settings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("back").resolveToItemWrapper()
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val menuDeque = ItemCatalogManager.getMenuStack(viewer)
            menuDeque.removeFirst()
            menuDeque.first.open()
        }
    }
}