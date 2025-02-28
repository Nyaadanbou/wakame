package cc.mewcraft.wakame.gui.catalog.item.menu

import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.catalog.item.init.ItemCatalogMenuSettings
import cc.mewcraft.wakame.gui.catalog.item.ItemCatalogMenuStack
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.ReloadableProperty
import net.kyori.adventure.text.Component
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
internal class MainMenu(

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) : ItemCatalogMenu {

    companion object {
        private val CATALOG_ITEM_POOL: HashMap<Identifier, CategoryItem> by ReloadableProperty { HashMap(32) }
    }

    private val settings = ItemCatalogMenuSettings.getMenuSettings("main")

    /**
     * 菜单的 [Gui].
     *
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `s`: search
     * - `x`: category
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('s', SearchItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        // 对 CategoryItem 进行缓存
        // TODO 权限检查代码美化
        builder.setContent(KoishRegistries.ITEM_CATEGORY.valueSequence.filter { category ->
            if (category.permission == null) return@filter true
            PermissionManager.hasPermission(viewer.world, viewer.uniqueId, category.permission).get()
        }.map { category ->
            CATALOG_ITEM_POOL.getOrPut(category.id) { CategoryItem(category) }
        }.toList())
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window = Window.single().apply {
        setGui(primaryGui)
        setViewer(viewer)
        setTitle(settings.title)
    }.build()

    override fun open() {
        primaryWindow.open()
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
    }

    /**
     * `上一页` 的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage())
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(primaryGui.currentPage + 1))
                    component("total_page", Component.text(primaryGui.pageAmount))
                }
            }
        }
    }

    /**
     * `下一页` 的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasNextPage())
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            return settings.getSlotDisplay("next_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(primaryGui.currentPage + 1))
                    component("total_page", Component.text(primaryGui.pageAmount))
                }
            }
        }
    }

    /**
     * `搜索` 的图标.
     */
    inner class SearchItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("search").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO 物品搜索功能
        }
    }

    /**
     * `类别` 的图标. 点击后打开一个特定的类别菜单.
     */
    inner class CategoryItem(
        private val category: Category,
    ) : AbstractItem() {

        private val itemProvider: ItemProvider = SlotDisplay.get(category.icon).resolveToItemWrapper()

        override fun getItemProvider(): ItemProvider {
            return itemProvider
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            ItemCatalogMenuStack.push(viewer, CategoryMenu(category, viewer))
        }

    }

}